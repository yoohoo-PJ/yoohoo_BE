package com.example.yoohoo_be.checklists.service;

import com.example.yoohoo_be.dashboard.domain.*;
import com.example.yoohoo_be.dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 도서 상호대차 매칭 알고리즘 서비스 (Interlibrary Loan Matcher).
 *
 * 유휴 도서의 이관 결정(RELOCATION) 시, 최적의 목적지 도서관을 추천합니다.
 * 4가지 지표(거리 감쇄, 수요도, 수요-공급 갭, 공간 효율)를 가중 합산하여
 * 상위 5개 도서관을 ill_recommendations 테이블에 저장합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IllRecommendationService {

    private final LibraryRepository libraryRepository;
    private final LibraryKdcStatRepository libraryKdcStatRepository;
    private final IllRecommendationRepository illRecommendationRepository;
    private final UscoreResultRepository uscoreResultRepository;
    private final KakaoDistanceClient kakaoDistanceClient;

    // ── 가중치 설정 ────────────────────────────────────
    private static final double W_DIST   = 0.30;
    private static final double W_DEMAND = 0.25;
    private static final double W_GAP    = 0.25;
    private static final double W_SPACE  = 0.20;

    // ── 하드 필터 기준 ──────────────────────────────────
    private static final double MAX_DISTANCE_KM = 15.0;
    private static final double HAVERSINE_PRE_FILTER_KM = 25.0; // Haversine 사전 필터 (API 호출 최소화)

    // ── 시그모이드 파라미터 ──────────────────────────────
    private static final double SIGMOID_ALPHA = 0.2;
    private static final double SIGMOID_D_MAX = 15.0;

    // ── 공간 효율성 파라미터 ─────────────────────────────
    private static final double AREA_PER_BOOK = 120.0;     // ㎡당 권장 장서 수
    private static final double SHELF_RATIO   = 0.35;      // 서가 배치 면적 비율
    private static final double SMALL_LIB_BONUS = 15.0;    // 작은도서관 보너스 가점
    private static final double FALLBACK_CAPACITY_FACTOR = 1.3; // 면적 결측 시 백업 한계

    private static final int MAX_RECOMMENDATIONS = 5;

    /**
     * 특정 도서에 대해 상호대차 추천 알고리즘을 실행하고 결과를 DB에 저장합니다.
     *
     * @param book 이관 대상 도서
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateRecommendations(Book book) {
        // 1. UscoreResult 조회 → 출발 도서관(origin) 확인
        Optional<UscoreResult> uscoreOpt = uscoreResultRepository.findByBookBookId(book.getBookId());
        if (uscoreOpt.isEmpty()) {
            log.warn("⚠ UscoreResult 없음 → 추천 생략. bookId={}", book.getBookId());
            return;
        }
        UscoreResult uscore = uscoreOpt.get();
        Library originLib = uscore.getLibrary();

        if (originLib.getLatitude() == null || originLib.getLongitude() == null) {
            log.warn("⚠ 출발 도서관 좌표 없음 → 추천 생략. libraryId={}", originLib.getLibraryId());
            return;
        }

        String kdcClass = book.getKdcClass(); // 도서의 KDC 대분류 (예: "8" = 문학)
        if (kdcClass == null || kdcClass.isBlank()) {
            kdcClass = "0"; // KDC 미분류 시 총류로 폴백
        }

        // 2. 기존 추천 결과 삭제 (재계산 대비)
        illRecommendationRepository.deleteByBook_BookId(book.getBookId());

        // 3. 후보 도서관 목록 (출발 도서관 제외)
        List<Library> allCandidates = libraryRepository.findAllByLibraryIdNot(originLib.getLibraryId());

        // 4. Haversine 사전 필터 (API 호출 횟수 최소화)
        List<Library> preFiltered = allCandidates.stream()
                .filter(dest -> dest.getLatitude() != null && dest.getLongitude() != null)
                .filter(dest -> KakaoDistanceClient.haversineKm(
                        originLib.getLatitude().doubleValue(), originLib.getLongitude().doubleValue(),
                        dest.getLatitude().doubleValue(), dest.getLongitude().doubleValue()
                ) <= HAVERSINE_PRE_FILTER_KM)
                .collect(Collectors.toList());

        log.info("📍 Haversine 사전 필터: 전체 {}개 → 후보 {}개", allCandidates.size(), preFiltered.size());

        // 5. 각 후보에 대해 4대 지표 계산
        List<ScoredCandidate> scoredList = new ArrayList<>();

        for (Library dest : preFiltered) {
            // ── 거리 계산 (카카오 API → Haversine 폴백) ──
            double distKm = kakaoDistanceClient.getDistanceKm(
                    originLib.getLatitude(), originLib.getLongitude(),
                    dest.getLatitude(), dest.getLongitude());

            // 하드 필터: 15km 초과 시 제외
            if (distKm > MAX_DISTANCE_KM) continue;

            // ① 거리 감쇄 점수 (F_dist)
            double fDist = 1.0 / (1.0 + Math.exp(SIGMOID_ALPHA * (distKm - SIGMOID_D_MAX)));

            // ② 수요도 점수 (S_demand)
            double sDemand = calculateDemandScore(dest.getLibraryId(), kdcClass);

            // ③ 수요-공급 갭 점수 (S_gap)
            double sGap = calculateGapScore(dest.getLibraryId(), kdcClass);

            // ④ 공간 효율성 점수 (S_space)
            double sSpace = calculateSpaceScore(dest);

            // 가중 합산
            double totalScore = W_DIST * fDist + W_DEMAND * sDemand + W_GAP * sGap + W_SPACE * sSpace;

            scoredList.add(new ScoredCandidate(dest, distKm, fDist, sDemand, sGap, sSpace, totalScore));
        }

        // 6. 점수 내림차순 정렬 → 상위 5개 저장
        scoredList.sort(Comparator.comparingDouble(ScoredCandidate::totalScore).reversed());

        int saveCount = Math.min(MAX_RECOMMENDATIONS, scoredList.size());
        for (int i = 0; i < saveCount; i++) {
            ScoredCandidate sc = scoredList.get(i);

            IllRecommendation rec = IllRecommendation.builder()
                    .uscoreResult(uscore)
                    .book(book)
                    .originLibrary(originLib)
                    .destLibrary(sc.library())
                    .rank((byte) (i + 1))
                    .matchingScore(toBD(sc.totalScore()))
                    .fDist(toBD(sc.fDist()))
                    .distanceKm(toBD(sc.distKm()))
                    .sDemand(toBD(sc.sDemand()))
                    .sGap(toBD(sc.sGap()))
                    .sSpace(toBD(sc.sSpace()))
                    .transferStatus(TransferStatus.PENDING)
                    .build();

            illRecommendationRepository.save(rec);
        }

        log.info("✅ 추천 완료: bookId={}, 추천 도서관 {}개 저장", book.getBookId(), saveCount);
    }

    // ──────────────────────────────────────────────────
    // 지표별 계산 메서드
    // ──────────────────────────────────────────────────

    /**
     * ② 수요도 점수 (S_demand)
     * 목적지 도서관의 해당 KDC 장르 대출 비율 × 100
     */
    private double calculateDemandScore(Integer libraryId, String kdcClass) {
        return libraryKdcStatRepository.findByLibrary_LibraryIdAndKdcClass(libraryId, kdcClass)
                .map(stat -> stat.getLoanRatio() != null
                        ? stat.getLoanRatio().doubleValue() * 100.0
                        : 0.0)
                .orElse(0.0);
    }

    /**
     * ③ 수요-공급 갭 점수 (S_gap)
     * 100 × max(0, (대출비율 - 소장비율) / 대출비율)
     */
    private double calculateGapScore(Integer libraryId, String kdcClass) {
        return libraryKdcStatRepository.findByLibrary_LibraryIdAndKdcClass(libraryId, kdcClass)
                .map(stat -> {
                    // demand_supply_gap 컬럼이 이미 계산되어 있으면 활용
                    if (stat.getDemandSupplyGap() != null) {
                        return Math.max(0.0, stat.getDemandSupplyGap().doubleValue() * 100.0);
                    }

                    double loanRatio = stat.getLoanRatio() != null ? stat.getLoanRatio().doubleValue() : 0.0;
                    double holdRatio = stat.getHoldingRatio() != null ? stat.getHoldingRatio().doubleValue() : 0.0;

                    if (loanRatio <= 0) return 0.0;
                    return 100.0 * Math.max(0.0, (loanRatio - holdRatio) / loanRatio);
                })
                .orElse(0.0);
    }

    /**
     * ④ 공간 효율성 점수 (S_space)
     * 적정 소장 권수 = 건물면적 × 120 × 0.35
     * S_space = max(0, 100 × (1 - 현재소장/적정소장))
     * 작은도서관이면 +15점 보너스
     */
    private double calculateSpaceScore(Library library) {
        int currentBooks = library.getTotalBooks() != null ? library.getTotalBooks() : 0;

        double capacity;
        if (library.getBuildingArea() != null && library.getBuildingArea().doubleValue() > 0) {
            capacity = library.getBuildingArea().doubleValue() * AREA_PER_BOOK * SHELF_RATIO;
        } else {
            // 면적 결측 시: 현재 소장 수의 1.3배를 백업 한계로 사용
            capacity = currentBooks * FALLBACK_CAPACITY_FACTOR;
        }

        if (capacity <= 0) capacity = 1; // 0 방지

        double score = 100.0 * Math.max(0.0, 1.0 - (double) currentBooks / capacity);

        // 작은도서관 보너스 가점
        if (library.getLibraryType() != null && library.getLibraryType().contains("작은도서관")) {
            score += SMALL_LIB_BONUS;
        }

        return Math.min(score, 100.0); // 최대 100점 제한
    }

    // ──────────────────────────────────────────────────
    // 유틸리티
    // ──────────────────────────────────────────────────

    private BigDecimal toBD(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 내부 점수 계산 결과를 담는 레코드
     */
    private record ScoredCandidate(
            Library library,
            double distKm,
            double fDist,
            double sDemand,
            double sGap,
            double sSpace,
            double totalScore
    ) {}
}

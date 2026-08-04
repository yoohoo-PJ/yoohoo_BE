package com.example.yoohoo_be.dashboard.service;

import com.example.yoohoo_be.dashboard.domain.Library;
import com.example.yoohoo_be.dashboard.domain.LibraryMonthlyStats;
import com.example.yoohoo_be.dashboard.domain.RegionalPopulation;
import com.example.yoohoo_be.dashboard.dto.DashboardCountDto;
import com.example.yoohoo_be.dashboard.dto.IdleBooksCountResponseDto;
import com.example.yoohoo_be.dashboard.dto.DamagePendingCountResponseDto;
import com.example.yoohoo_be.dashboard.dto.MonthlyLoanDto;
import com.example.yoohoo_be.dashboard.dto.NetworkDistanceDto;
import com.example.yoohoo_be.dashboard.dto.RegionalPopulationDto;
import com.example.yoohoo_be.dashboard.repository.LibraryMonthlyStatsRepository;
import com.example.yoohoo_be.dashboard.repository.LibraryRepository;
import com.example.yoohoo_be.dashboard.repository.RegionalPopulationRepository;
import com.example.yoohoo_be.dashboard.repository.UscoreResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RegionalPopulationRepository regionalPopulationRepository;
    private final LibraryRepository libraryRepository;
    private final LibraryMonthlyStatsRepository monthlyStatsRepository;
    private final UscoreResultRepository uscoreResultRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    public RegionalPopulationDto getUserDistribution() {
        RegionalPopulation entity = regionalPopulationRepository.findByRegionNameContaining("장안구")
                .orElseThrow(() -> new IllegalArgumentException("해당 지역의 인원 분포 통계 데이터를 찾을 수 없습니다."));
        
        int total = getSafeValue(entity.getTotalPopulation());

        // 60세 이상 분포
        int age60PlusCount = getSafeValue(entity.getAge60To69()) + getSafeValue(entity.getAge70To79()) + 
                             getSafeValue(entity.getAge80To89()) + getSafeValue(entity.getAge90To99()) + 
                             getSafeValue(entity.getAge100Plus());

        RegionalPopulationDto.AgeDistributionDto distributionDto = RegionalPopulationDto.AgeDistributionDto.builder()
                .ageUnder10(createAgeGroup(entity.getAge0To9(), total))
                .age10s(createAgeGroup(entity.getAge10To19(), total))
                .age20s(createAgeGroup(entity.getAge20To29(), total))
                .age30s(createAgeGroup(entity.getAge30To39(), total))
                .age40s(createAgeGroup(entity.getAge40To49(), total))
                .age50s(createAgeGroup(entity.getAge50To59(), total))
                .age60Plus(createAgeGroup(age60PlusCount, total))
                .build();

        return RegionalPopulationDto.builder()
                .districtName("장안구")
                .totalPopulation(total)
                .ageDistribution(distributionDto)
                .build();
    }

    private RegionalPopulationDto.AgeGroupDto createAgeGroup(Integer count, int total) {
        int safeCount = getSafeValue(count);
        double percentage = total == 0 ? 0.0 : Math.round(((double) safeCount / total) * 1000.0) / 10.0;
        return new RegionalPopulationDto.AgeGroupDto(percentage, safeCount);
    }

    private int getSafeValue(Integer value) {
        return value == null ? 0 : value;
    }

    // 도서관 네트워크 거리 조회 (카카오 API)
    public List<NetworkDistanceDto> getRealDistancesFromCentral() {
        // 1. 기준 도서관 (경기도교육청중앙도서관) 찾기
        Library origin = libraryRepository.findByLibraryName("경기도교육청중앙도서관")
                .orElseThrow(() -> new IllegalArgumentException("경기도교육청중앙도서관 정보를 찾을 수 없습니다."));

        // 2. 전체 도서관 목록 불러오기
        List<Library> allLibraries = libraryRepository.findAll();

        List<NetworkDistanceDto> distances = new ArrayList<>();

        for (Library dest : allLibraries) {
            // 본인은 제외
            if (dest.getLibraryId().equals(origin.getLibraryId())) continue;
            // 위경도 값이 없는 도서관 제외
            if (dest.getLongitude() == null || dest.getLatitude() == null) continue;

            // 3. 카카오 API 호출하여 거리 구하기
            double distanceKm = fetchKakaoDistance(origin, dest);

            distances.add(NetworkDistanceDto.builder()
                    .libraryName(dest.getLibraryName())
                    .address(formatAddress(dest.getAddress()))
                    .bookCount(dest.getTotalBooks() == null ? 0 : dest.getTotalBooks())
                    .length(distanceKm)
                    .build());
        }

        // 4. 거리가 짧은 순으로 정렬하여 전체 리스트 생성
        List<NetworkDistanceDto> sortedDistances = distances.stream()
                .filter(dto -> dto.getLength() >= 0) // 에러로 인해 -1이 된 값은 제외
                .sorted(Comparator.comparingDouble(NetworkDistanceDto::getLength))
                .collect(Collectors.toList());

        // 상위 5개 추출
        List<NetworkDistanceDto> top5 = new ArrayList<>(sortedDistances.stream().limit(5).collect(Collectors.toList()));

        // 5. '작은도서관'이 포함되어 있는지 확인
        boolean hasSmallLibrary = top5.stream().anyMatch(dto -> dto.getLibraryName().contains("작은도서관"));

        // 6. '작은도서관'이 없다면 가장 가까운 작은도서관을 찾아 5번째 도서관과 교체
        if (!hasSmallLibrary && sortedDistances.size() > 5) {
            NetworkDistanceDto closestSmallLib = sortedDistances.stream()
                    .filter(dto -> dto.getLibraryName().contains("작은도서관"))
                    .findFirst()
                    .orElse(null);

            if (closestSmallLib != null) {
                top5.remove(top5.size() - 1); // 가장 먼 도서관 제거
                top5.add(closestSmallLib); // 작은도서관 추가
                top5.sort(Comparator.comparingDouble(NetworkDistanceDto::getLength)); // 다시 거리순 정렬
            }
        }

        return top5;
    }

    private double fetchKakaoDistance(Library origin, Library dest) {
        try {
            String url = String.format("https://apis-navi.kakaomobility.com/v1/directions?origin=%s,%s&destination=%s,%s",
                    origin.getLongitude(), origin.getLatitude(),
                    dest.getLongitude(), dest.getLatitude());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("routes")) {
                List<Map<String, Object>> routes = (List<Map<String, Object>>) body.get("routes");
                if (!routes.isEmpty()) {
                    Map<String, Object> summary = (Map<String, Object>) routes.get(0).get("summary");
                    Integer distanceMeter = (Integer) summary.get("distance");
                    // 미터(m)를 킬로미터(km)로 변환 후 소수점 1자리 반올림
                    return Math.round((distanceMeter / 1000.0) * 10.0) / 10.0;
                }
            }
        } catch (Exception e) {
            System.err.println("카카오 API 호출 실패: " + dest.getLibraryName());
        }
        return -1.0; // 실패 시 임시 값
    }

    private String formatAddress(String rawAddress) {
        if (rawAddress == null || rawAddress.trim().isEmpty()) {
            return "주소 미상";
        }
        
        String[] parts = rawAddress.split("\\s+");
        String city = null;
        
        for (String part : parts) {
            if (part.endsWith("구")) {
                return part; // 1순위: '구' 반환 (예: 팔달구)
            }
            if (part.endsWith("시")) {
                city = part; // 2순위 후보 저장 (예: 의왕시)
            }
        }
        
        // '구'가 없으면 '시' 반환, '시'도 없으면 원본 그대로 반환
        return city != null ? city : rawAddress;
    }

    // 도서관 월별 대출 현황 조회 (12개월)
    public List<MonthlyLoanDto> getMonthlyLoans() {
        // 1. 기준 도서관 (경기도교육청중앙도서관) 찾기
        Library origin = libraryRepository.findByLibraryName("경기도교육청중앙도서관")
                .orElseThrow(() -> new IllegalArgumentException("경기도교육청중앙도서관 정보를 찾을 수 없습니다."));

        // 2. DB에서 최근 12개월 치 데이터 조회 (최신순)
        List<LibraryMonthlyStats> stats = monthlyStatsRepository.findTop12ByLibraryOrderByStatYearDescStatMonthDesc(origin);

        // 3. 차트 표시를 위해 과거->최신 순으로 다시 뒤집기 (오름차순 정렬)
        Collections.reverse(stats);

        // 4. DTO 변환
        return stats.stream().map(stat -> {
            String yearMonth = String.format("%04d-%02d", stat.getStatYear(), stat.getStatMonth());
            return MonthlyLoanDto.builder()
                    .yearMonth(yearMonth)
                    .totalBooks(stat.getTotalBooks() == null ? 0 : stat.getTotalBooks())
                    .totalLoans(stat.getTotalLoans() == null ? 0 : stat.getTotalLoans())
                    .turnoverRate(stat.getTurnoverRate() == null ? 0.0 : stat.getTurnoverRate().doubleValue())
                    .build();
        }).collect(Collectors.toList());
    }

    // 유휴화 도서 수 조회
    public IdleBooksCountResponseDto getIdleBooksCount() {
        Library origin = libraryRepository.findByLibraryName("경기도교육청중앙도서관")
                .orElseThrow(() -> new IllegalArgumentException("경기도교육청중앙도서관 정보를 찾을 수 없습니다."));

        long currentCount = uscoreResultRepository.countByLibraryAndIsIdle(origin.getLibraryId());
        long lastMonthCount = 200; // TODO: 유휴화 도서의 월별 스냅샷 테이블이 없으므로 현재는 임의의 값 지정

        int percentageChange = lastMonthCount == 0 ? 0 : (int) Math.round((double)(currentCount - lastMonthCount) / lastMonthCount * 100);

        return IdleBooksCountResponseDto.builder()
                .currentMonthCount(currentCount)
                .lastMonthCount(lastMonthCount)
                .percentageChange(percentageChange)
                .build();
    }

    // 파손 심사 대기 수 조회
    public DamagePendingCountResponseDto getDamagePendingCount() {
        Library origin = libraryRepository.findByLibraryName("경기도교육청중앙도서관")
                .orElseThrow(() -> new IllegalArgumentException("경기도교육청중앙도서관 정보를 찾을 수 없습니다."));

        long currentCount = uscoreResultRepository.countDamagePendingByLibrary(origin.getLibraryId());
        long lastMonthCount = 404; // TODO: 파손 심사 대기의 월별 스냅샷 테이블이 없으므로 현재는 임의의 값 지정

        long countChange = currentCount - lastMonthCount;

        return DamagePendingCountResponseDto.builder()
                .currentMonthCount(currentCount)
                .lastMonthCount(lastMonthCount)
                .countChange(countChange)
                .build();
    }

    // 이관 검토 대기 수 조회
    public DashboardCountDto getTransferPendingCount() {
        Library origin = libraryRepository.findByLibraryName("경기도교육청중앙도서관")
                .orElseThrow(() -> new IllegalArgumentException("경기도교육청중앙도서관 정보를 찾을 수 없습니다."));

        long count = uscoreResultRepository.countTransferPendingByLibrary(
                origin.getLibraryId(), 
                com.example.yoohoo_be.dashboard.domain.InspectionStatus.PASS
        );
        return new DashboardCountDto(count);
    }
}
package com.example.yoohoo_be.transfer.service;

import com.example.yoohoo_be.dashboard.domain.IllRecommendation;
import com.example.yoohoo_be.dashboard.domain.TransferStatus;
import com.example.yoohoo_be.dashboard.repository.IllRecommendationRepository;
import com.example.yoohoo_be.dashboard.repository.LibraryRepository;
import com.example.yoohoo_be.dashboard.domain.Library;
import com.example.yoohoo_be.transfer.dto.TransferAlternativeDto;
import com.example.yoohoo_be.transfer.dto.TransferPageResponseDto;
import com.example.yoohoo_be.transfer.dto.TransferResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final IllRecommendationRepository recommendationRepository;
    private final LibraryRepository libraryRepository;

    @Transactional(readOnly = true)
    public TransferPageResponseDto getTransferList(List<TransferStatus> statuses, Pageable pageable, String myLibraryName) {

        Library myLibrary = libraryRepository.findByLibraryName(myLibraryName)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 도서관 정보를 찾을 수 없습니다."));
        Integer myLibraryId = myLibrary.getLibraryId();

        // 1. 메인 추천 경로 (rank=1) 조회
        Page<IllRecommendation> mainPage = recommendationRepository.findMainRecommendationsByStatuses(statuses, pageable);

        // 2. 메인 추천에 해당하는 UscoreResult ID 목록 추출
        List<Long> uscoreResultIds = mainPage.getContent().stream()
                .map(r -> r.getUscoreResult().getResultId())
                .collect(Collectors.toList());

        // 3. 해당 UscoreResult ID들에 대한 대안 경로 (rank > 1) 조회
        List<IllRecommendation> allAlternatives = uscoreResultIds.isEmpty() ? List.of() :
                recommendationRepository.findAlternativesByUscoreResultIds(statuses, uscoreResultIds);

        // 4. 대안 경로를 UscoreResult ID 기준으로 그룹화
        Map<Long, List<IllRecommendation>> alternativesMap = allAlternatives.stream()
                .collect(Collectors.groupingBy(a -> a.getUscoreResult().getResultId()));

        // 5. 요약 정보 (summary) 계산
        int totalSent = recommendationRepository.countByTransferStatusInAndOriginLibrary_LibraryId(statuses, myLibraryId);
        int totalReceived = recommendationRepository.countByTransferStatusInAndDestLibrary_LibraryId(statuses, myLibraryId);

        TransferPageResponseDto.Summary summary = TransferPageResponseDto.Summary.builder()
                .totalPending((int) mainPage.getTotalElements())
                .totalSent(totalSent)
                .totalReceived(totalReceived)
                .build();

        // 6. 응답 DTO 조립
        List<TransferResponseDto> content = mainPage.getContent().stream().map(mainRec -> {
            Long resultId = mainRec.getUscoreResult().getResultId();
            List<IllRecommendation> alts = alternativesMap.getOrDefault(resultId, List.of());

            List<TransferAlternativeDto> altDtos = alts.stream().map(alt -> TransferAlternativeDto.builder()
                    .recommendationId(alt.getRecommendationId())
                    .originLibrary(alt.getOriginLibrary().getLibraryName())
                    .destLibrary(alt.getDestLibrary().getLibraryName())
                    .distanceKm(alt.getDistanceKm())
                    .matchingScore(alt.getMatchingScore())
                    .direction(getDirection(alt, myLibraryId))
                    .status(alt.getTransferStatus().name())
                    .scoreDetails(TransferResponseDto.ScoreDetails.builder()
                            .distanceDecay(alt.getFDist())
                            .bookDemand(alt.getSDemand())
                            .shortageResolution(alt.getSGap())
                            .spaceEfficiency(alt.getSSpace())
                            .build())
                    .build()).collect(Collectors.toList());

            return TransferResponseDto.builder()
                    .recommendationId(mainRec.getRecommendationId())
                    .bookTitle(mainRec.getBook().getTitle())
                    .genre(convertKdcToGenre(mainRec.getBook().getKdcClass()))
                    .originLibrary(mainRec.getOriginLibrary().getLibraryName())
                    .destLibrary(mainRec.getDestLibrary().getLibraryName())
                    .distanceKm(mainRec.getDistanceKm())
                    .matchingScore(mainRec.getMatchingScore())
                    .direction(getDirection(mainRec, myLibraryId))
                    .status(mainRec.getTransferStatus().name())
                    .scoreDetails(TransferResponseDto.ScoreDetails.builder()
                            .distanceDecay(mainRec.getFDist())
                            .bookDemand(mainRec.getSDemand())
                            .shortageResolution(mainRec.getSGap())
                            .spaceEfficiency(mainRec.getSSpace())
                            .build())
                    .alternatives(altDtos)
                    .build();
        }).collect(Collectors.toList());

        return TransferPageResponseDto.builder()
                .summary(summary)
                .content(content)
                .pageable(TransferPageResponseDto.PageableInfo.builder()
                        .pageNumber(mainPage.getNumber())
                        .pageSize(mainPage.getSize())
                        .build())
                .totalElements(mainPage.getTotalElements())
                .totalPages(mainPage.getTotalPages())
                .build();
    }

    @Transactional
    public void executeTransfer(Long recommendationId) {
        IllRecommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 처리된 이관 요청입니다."));
        
        // 이관 실행 처리
        recommendation.setTransferStatus(TransferStatus.IN_TRANSIT);
        recommendation.setShippedAt(java.time.LocalDateTime.now());
    }

    private String getDirection(IllRecommendation rec, Integer myLibraryId) {
        if (rec.getOriginLibrary().getLibraryId().equals(myLibraryId)) {
            return "발신";
        } else if (rec.getDestLibrary().getLibraryId().equals(myLibraryId)) {
            return "수신";
        }
        return "무관";
    }

    private String convertKdcToGenre(String kdcClass) {
        if (kdcClass == null || kdcClass.isEmpty()) return "기타";
        char c = kdcClass.charAt(0);
        return switch (c) {
            case '0' -> "총류";
            case '1' -> "철학";
            case '2' -> "종교";
            case '3' -> "사회과학";
            case '4' -> "자연과학";
            case '5' -> "기술과학";
            case '6' -> "예술";
            case '7' -> "언어";
            case '8' -> "문학";
            case '9' -> "역사";
            default -> "기타";
        };
    }
}

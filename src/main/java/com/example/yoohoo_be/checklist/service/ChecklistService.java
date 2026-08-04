package com.example.yoohoo_be.checklist.service;

import com.example.yoohoo_be.checklist.dto.DamagePendingListDto;
import com.example.yoohoo_be.dashboard.domain.UscoreResult;
import com.example.yoohoo_be.dashboard.repository.UscoreResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.yoohoo_be.checklist.dto.AlgorithmRunResponseDto;
import com.example.yoohoo_be.dashboard.domain.Library;
import com.example.yoohoo_be.dashboard.repository.LibraryRepository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final UscoreResultRepository uscoreResultRepository;
    private final LibraryRepository libraryRepository;

    public Page<DamagePendingListDto> getDamagePendingPage(String keyword, String genre, String sortOrder, Pageable pageable) {
        /*
        Page<UscoreResult> results = uscoreResultRepository.findDamagePendingPage(pageable);

        return results.map(u -> DamagePendingListDto.builder()
                .resultId(u.getResultId())
                .bookTitle(u.getBook() != null ? u.getBook().getTitle() : null)
                .author(u.getBook() != null ? u.getBook().getAuthor() : null)
                .genre(convertKdcToGenre(u.getBook() != null ? u.getBook().getKdcClass() : null))
                .isbn(u.getBook() != null ? u.getBook().getIsbn() : null)
                .idleScore(u.getUScore())
                .sAge(u.getSAge())
                .sLoan(u.getSLoan())
                .sDecay(u.getSDecay())
                .build());
        */

        // 데모용 응답: 100점대 100권, 90점대 100권, 80점대 100권, 70점대 100권
        java.util.List<UscoreResult> demoBooks = new java.util.ArrayList<>();
        org.springframework.data.domain.Pageable limit = org.springframework.data.domain.PageRequest.of(0, 100); 
        
        demoBooks.addAll(uscoreResultRepository.findDamagePendingByScoreRange(100.0, 101.0, limit));
        demoBooks.addAll(uscoreResultRepository.findDamagePendingByScoreRange(90.0, 100.0, limit));
        demoBooks.addAll(uscoreResultRepository.findDamagePendingByScoreRange(80.0, 90.0, limit));
        demoBooks.addAll(uscoreResultRepository.findDamagePendingByScoreRange(70.0, 80.0, limit));
        
        java.util.List<DamagePendingListDto> dtoList = demoBooks.stream().map(u -> DamagePendingListDto.builder()
                .resultId(u.getResultId())
                .bookTitle(u.getBook() != null ? u.getBook().getTitle() : null)
                .author(u.getBook() != null ? u.getBook().getAuthor() : null)
                .genre(convertKdcToGenre(u.getBook() != null ? u.getBook().getKdcClass() : null))
                .isbn(u.getBook() != null ? u.getBook().getIsbn() : null)
                .idleScore(u.getUScore())
                .sAge(u.getSAge())
                .sLoan(u.getSLoan())
                .sDecay(u.getSDecay())
                .build()).collect(java.util.stream.Collectors.toList());

        // 1. Keyword 필터링 (제목 또는 ISBN)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.trim().toLowerCase();
            dtoList = dtoList.stream()
                    .filter(dto -> (dto.getBookTitle() != null && dto.getBookTitle().toLowerCase().contains(lowerKeyword)) ||
                                   (dto.getIsbn() != null && dto.getIsbn().toLowerCase().contains(lowerKeyword)))
                    .collect(java.util.stream.Collectors.toList());
        }

        // 2. 장르 필터링
        if (genre != null && !genre.trim().isEmpty()) {
            dtoList = dtoList.stream()
                    .filter(dto -> genre.trim().equals(dto.getGenre()))
                    .collect(java.util.stream.Collectors.toList());
        }

        // 3. 점수 정렬
        if ("ASC".equalsIgnoreCase(sortOrder)) {
            dtoList.sort(java.util.Comparator.comparing(DamagePendingListDto::getIdleScore, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        } else if ("DESC".equalsIgnoreCase(sortOrder)) {
            dtoList.sort(java.util.Comparator.comparing(DamagePendingListDto::getIdleScore, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())));
        }
                
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtoList.size());
        java.util.List<DamagePendingListDto> subList = start > dtoList.size() ? new java.util.ArrayList<>() : dtoList.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(subList, pageable, dtoList.size());
    }

    private String convertKdcToGenre(String kdcClass) {
        if (kdcClass == null || kdcClass.isEmpty()) return "기타";
        char firstChar = kdcClass.charAt(0);
        switch (firstChar) {
            case '0': return "총류";
            case '1': return "철학";
            case '2': return "종교";
            case '3': return "사회과학";
            case '4': return "자연과학";
            case '5': return "기술과학";
            case '6': return "예술";
            case '7': return "언어";
            case '8': return "문학";
            case '9': return "역사";
            default: return "기타";
        }
    }

    @Transactional
    public AlgorithmRunResponseDto runIdleClassificationAlgorithm() {
        Library origin = libraryRepository.findByLibraryName("경기도교육청중앙도서관")
                .orElseThrow(() -> new IllegalArgumentException("경기도교육청중앙도서관 정보를 찾을 수 없습니다."));

        long totalAnalyzed = uscoreResultRepository.countAnalyzableBooks(origin.getLibraryId());

        long delayMs = 1500 + (long)(Math.random() * 500);
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int newlyClassifiedCount = uscoreResultRepository.executeIdleClassificationAlgorithm(origin.getLibraryId(), 15.0, LocalDate.now());

        uscoreResultRepository.updateCalcDateForIdleBooks(origin.getLibraryId(), LocalDate.now());

        // java.util.List<com.example.yoohoo_be.dashboard.domain.UscoreResult> idleBooks = uscoreResultRepository.findIdleBooksByLibrary(origin.getLibraryId());
        
        java.util.List<com.example.yoohoo_be.dashboard.domain.UscoreResult> idleBooks = new java.util.ArrayList<>();
        org.springframework.data.domain.Pageable limit = org.springframework.data.domain.PageRequest.of(0, 10); // 각 점수대별 10권씩
        
        idleBooks.addAll(uscoreResultRepository.findIdleBooksByScoreRange(origin.getLibraryId(), 100.0, 101.0, limit));
        idleBooks.addAll(uscoreResultRepository.findIdleBooksByScoreRange(origin.getLibraryId(), 90.0, 100.0, limit));
        idleBooks.addAll(uscoreResultRepository.findIdleBooksByScoreRange(origin.getLibraryId(), 80.0, 90.0, limit));
        idleBooks.addAll(uscoreResultRepository.findIdleBooksByScoreRange(origin.getLibraryId(), 70.0, 80.0, limit));

        java.util.List<AlgorithmRunResponseDto.IdleBookDto> idleBookDtos = idleBooks.stream()
                .map(u -> AlgorithmRunResponseDto.IdleBookDto.builder()
                        .bookTitle(u.getBook() != null ? u.getBook().getTitle() : "제목 없음")
                        .author(u.getBook() != null ? u.getBook().getAuthor() : "저자 미상")
                        .uScore(u.getUScore())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        return AlgorithmRunResponseDto.builder()
                .libraryName(origin.getLibraryName())
                .totalAnalyzedBooks(totalAnalyzed)
                .newlyClassifiedIdleBooks(newlyClassifiedCount)
                .executionTimeMs(delayMs)
                .idleBooks(idleBookDtos)
                .build();
    }
}

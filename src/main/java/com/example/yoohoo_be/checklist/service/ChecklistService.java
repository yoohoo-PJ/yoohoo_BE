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

        String kdcPrefix = null;
        if (genre != null && !genre.trim().isEmpty() && !"기타".equals(genre.trim())) {
            switch (genre.trim()) {
                case "총류": kdcPrefix = "0"; break;
                case "철학": kdcPrefix = "1"; break;
                case "종교": kdcPrefix = "2"; break;
                case "사회과학": kdcPrefix = "3"; break;
                case "자연과학": kdcPrefix = "4"; break;
                case "기술과학": kdcPrefix = "5"; break;
                case "예술": kdcPrefix = "6"; break;
                case "언어": kdcPrefix = "7"; break;
                case "문학": kdcPrefix = "8"; break;
                case "역사": kdcPrefix = "9"; break;
            }
        }

        String safeKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "uScore");
        if ("ASC".equalsIgnoreCase(sortOrder)) {
            sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "uScore");
        }
        Pageable sortedPageable = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<UscoreResult> resultsPage = uscoreResultRepository.findDamagePendingWithFilters(safeKeyword, kdcPrefix, sortedPageable);

        return resultsPage.map(u -> DamagePendingListDto.builder()
                .resultId(u.getResultId())
                .bookId(u.getBook() != null ? u.getBook().getBookId() : null)
                .bookTitle(u.getBook() != null ? u.getBook().getTitle() : null)
                .author(u.getBook() != null ? u.getBook().getAuthor() : null)
                .genre(convertKdcToGenre(u.getBook() != null ? u.getBook().getKdcClass() : null))
                .isbn(u.getBook() != null ? u.getBook().getIsbn() : null)
                .idleScore(u.getUScore())
                .sAge(u.getSAge())
                .sLoan(u.getSLoan())
                .sDecay(u.getSDecay())
                .build());
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

        int newlyClassifiedCount = uscoreResultRepository.executeIdleClassificationAlgorithm(origin.getLibraryId(), 60.0, LocalDate.now());

        uscoreResultRepository.updateCalcDateForIdleBooks(origin.getLibraryId(), LocalDate.now());

        org.springframework.data.domain.Pageable limit = org.springframework.data.domain.PageRequest.of(0, 100, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "uScore"));
        java.util.List<com.example.yoohoo_be.dashboard.domain.UscoreResult> idleBooks = uscoreResultRepository.findIdleBooksByScoreRange(origin.getLibraryId(), 0.0, 101.0, limit);

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

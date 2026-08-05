package com.example.yoohoo_be.checklists.service;

import com.example.yoohoo_be.dashboard.domain.Book;
import com.example.yoohoo_be.checklists.domain.BookCheckBatch;
import com.example.yoohoo_be.dashboard.domain.BookStatus;
import com.example.yoohoo_be.dashboard.domain.Library;
import com.example.yoohoo_be.checklists.dto.BookCheckCompletedListResponseDto;
import com.example.yoohoo_be.checklists.dto.BookSummaryResponseDto;
import com.example.yoohoo_be.checklists.dto.BookWearStatusDetailResponseDto;
import com.example.yoohoo_be.checklists.dto.DiscardQuotaDto;
import com.example.yoohoo_be.checklists.dto.DiscardedBookListResponseDto;
import com.example.yoohoo_be.common.exception.ResourceNotFoundException;
import com.example.yoohoo_be.checklists.repository.BookCheckBatchRepository;
import com.example.yoohoo_be.dashboard.repository.BookRepository;
import com.example.yoohoo_be.dashboard.repository.LibraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookWearStatusService {

    private final BookCheckBatchRepository bookCheckBatchRepository;
    private final BookRepository bookRepository; // [추가됨] 폐기/이관/보존 결정 및 상태별 목록 조회용
    private final LibraryRepository libraryRepository;

    // 도서관법 시행령 [별표 7] 제3호: 도서관자료의 폐기 및 제적의 범위는 연간 전체 장서의 100분의 7을 초과할 수 없다.
    public static final double DISCARD_CAP_RATIO = 0.07;
    // 시연 기준 도서관 (다른 서비스 로직과 동일하게 고정)
    public static final String TARGET_LIBRARY_NAME = "경기도교육청중앙도서관";

    /**
     * 1. [마모 처리 현황 - 첫 번째 화면] 점검 완료 도서 전체 목록 조회
     */
    public List<BookCheckCompletedListResponseDto> getCompletedBookChecks() {
        return bookCheckBatchRepository.findAllCompletedOrderByCheckedDateDesc()
                .stream()
                .map(this::toCompletedListDto)
                .collect(Collectors.toList());
    }

    /**
     * 2. [마모 처리 현황 - 두 번째 상세 모달 화면] 특정 도서의 마모 상태 점검 상세 결과 조회 (bookId 기준)
     */
    public BookWearStatusDetailResponseDto getWearStatusDetailByBookId(Integer bookId) {
        BookCheckBatch batch = bookCheckBatchRepository.findLatestByBookId(bookId)
                .orElseThrow(() -> new IllegalArgumentException("해당 도서의 점검 내역이 존재하지 않습니다. Book ID: " + bookId));

        var bookInfo = BookWearStatusDetailResponseDto.BookInfoDto.builder()
                .bookId(batch.getBook().getBookId())
                .title(batch.getBook().getTitle())
                .author(batch.getBook().getAuthor())
                .publisher(batch.getBook().getPublisher())
                .isbn(batch.getBook().getIsbn())
                .kdcCode(batch.getBook().getKdcCode())
                .kdcClass(batch.getBook().getKdcClass())
                .genre(convertKdcToGenre(batch.getBook().getKdcClass()))
                .callNumber(batch.getBook().getCallNumber())
                .coverUrl(batch.getBook().getCoverUrl())
                .status(batch.getBook().getStatus().name())
                .build();

        List<BookWearStatusDetailResponseDto.CheckResultItemDto> checkResults =
                batch.getItems().stream()
                        .map(item -> BookWearStatusDetailResponseDto.CheckResultItemDto.builder()
                                .checkItemId(item.getCheckItem().getId())
                                .title(item.getCheckItem().getTitle())
                                .isPassed(item.getIsPassed())
                                .itemScore(item.getItemScore())
                                .note(item.getNote())
                                .build())
                        .collect(Collectors.toList());

        return BookWearStatusDetailResponseDto.builder()
                .resultBatchId(batch.getId())
                .checkedDate(batch.getCheckedDate())
                .librarianCode(batch.getLibrarianCode())
                .totalScore(batch.getTotalScore())
                .bookInfo(bookInfo)
                .checkResults(checkResults)
                .build();
    }


    /**
     * 4. [추가됨] 특정 상태(예: TRANSFERRED, PRESERVED, DISCARDED)의 도서 목록 조회
     * "다른 페이지에서 이관 결정된 도서 목록을 보여준다" 같은 요구사항을 위해 필요.
     */
    public List<BookSummaryResponseDto> getBooksByStatus(BookStatus status) {
        return bookRepository.findAllByStatus(status)
                .stream()
                .map(book -> BookSummaryResponseDto.builder()
                        .bookId(book.getBookId())
                        .title(book.getTitle())
                        .author(book.getAuthor())
                        .publisher(book.getPublisher())
                        .isbn(book.getIsbn())
                        .kdcCode(book.getKdcCode())
                        .kdcClass(book.getKdcClass())
                        .callNumber(book.getCallNumber())
                        .coverUrl(book.getCoverUrl())
                        .status(book.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 5. [신규] 폐기 도서 목록 화면 - 폐기 확정 도서 목록 + 연간 폐기 상한(7%) 현황
     * [GET] /api/checklists/discarded
     */
    public DiscardedBookListResponseDto getDiscardedBooksWithQuota() {
        List<BookSummaryResponseDto> discardedBooks = getBooksByStatus(BookStatus.DISCARDED);
        DiscardQuotaDto quota = calculateDiscardQuota();

        return DiscardedBookListResponseDto.builder()
                .books(discardedBooks)
                .quota(quota)
                .build();
    }

    /**
     * 도서관법 시행령 [별표 7] 제3호 기준 연간 폐기 상한(전체 장서 × 7%) 현황을 계산한다.
     */
    public DiscardQuotaDto calculateDiscardQuota() {
        Library library = libraryRepository.findByLibraryName(TARGET_LIBRARY_NAME)
                .orElseThrow(() -> new ResourceNotFoundException(TARGET_LIBRARY_NAME + " 정보를 찾을 수 없습니다."));

        int totalBooks = library.getTotalBooks() != null ? library.getTotalBooks() : 0;
        long discardedCount = bookRepository.countByStatus(BookStatus.DISCARDED);
        long capCount = (long) Math.floor(totalBooks * DISCARD_CAP_RATIO);
        long remaining = Math.max(0, capCount - discardedCount);

        return DiscardQuotaDto.builder()
                .totalBooks(totalBooks)
                .discardedCount(discardedCount)
                .capCount(capCount)
                .capRatio(DISCARD_CAP_RATIO)
                .remaining(remaining)
                .capReached(discardedCount >= capCount)
                .build();
    }

    private BookCheckCompletedListResponseDto toCompletedListDto(BookCheckBatch batch) {
        Book book = batch.getBook();
        return BookCheckCompletedListResponseDto.builder()
                .resultBatchId(batch.getId())
                .bookId(book.getBookId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .isbn(book.getIsbn())
                .kdcCode(book.getKdcCode())
                .kdcClass(book.getKdcClass())
                .genre(convertKdcToGenre(book.getKdcClass()))
                .callNumber(book.getCallNumber())
                .coverUrl(book.getCoverUrl())
                .checkedDate(batch.getCheckedDate())
                .librarianName(batch.getLibrarianCode())
                .totalScore(batch.getTotalScore())
                .status(book.getStatus().name())
                .build();
    }

    private String convertKdcToGenre(String kdcClass) {
        if (kdcClass == null) return "미분류";
        if (kdcClass.startsWith("0")) return "총류";
        if (kdcClass.startsWith("1")) return "철학";
        if (kdcClass.startsWith("2")) return "종교";
        if (kdcClass.startsWith("3")) return "사회과학";
        if (kdcClass.startsWith("4")) return "자연과학";
        if (kdcClass.startsWith("5")) return "기술과학";
        if (kdcClass.startsWith("6")) return "예술";
        if (kdcClass.startsWith("7")) return "언어";
        if (kdcClass.startsWith("8")) return "문학";
        if (kdcClass.startsWith("9")) return "역사";
        return "미분류";
    }
}
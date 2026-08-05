package com.example.yoohoo_be.checklists.service;

import com.example.yoohoo_be.dashboard.domain.Book;
import com.example.yoohoo_be.checklists.domain.BookCheckBatch;
import com.example.yoohoo_be.dashboard.domain.BookSnapshot;
import com.example.yoohoo_be.dashboard.domain.BookStatus;
import com.example.yoohoo_be.dashboard.domain.Library;
import com.example.yoohoo_be.dashboard.domain.LibraryMonthlyStats;
import com.example.yoohoo_be.checklists.dto.BookCheckCompletedListResponseDto;
import com.example.yoohoo_be.checklists.dto.BookMonthlyLoanPointDto;
import com.example.yoohoo_be.checklists.dto.BookSummaryResponseDto;
import com.example.yoohoo_be.checklists.dto.BookWearStatusDetailResponseDto;
import com.example.yoohoo_be.checklists.dto.DiscardQuotaDto;
import com.example.yoohoo_be.checklists.dto.DiscardedBookListResponseDto;
import com.example.yoohoo_be.common.exception.ResourceNotFoundException;
import com.example.yoohoo_be.checklists.repository.BookCheckBatchRepository;
import com.example.yoohoo_be.dashboard.repository.BookRepository;
import com.example.yoohoo_be.dashboard.repository.BookSnapshotRepository;
import com.example.yoohoo_be.dashboard.repository.LibraryRepository;
import com.example.yoohoo_be.dashboard.repository.LibraryMonthlyStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookWearStatusService {

    private final BookCheckBatchRepository bookCheckBatchRepository;
    private final BookRepository bookRepository; // [추가됨] 폐기/이관/보존 결정 및 상태별 목록 조회용
    private final LibraryRepository libraryRepository;
    private final LibraryMonthlyStatsRepository monthlyStatsRepository;
    private final BookSnapshotRepository bookSnapshotRepository;

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
     * 전체 장서 수(totalBooks)는 개요 화면의 "소장 도서 수"와 동일하게, 해당 도서관의
     * 가장 최근 월별 통계(LibraryMonthlyStats)의 total_books 값을 기준으로 한다.
     */
    public DiscardQuotaDto calculateDiscardQuota() {
        Library library = libraryRepository.findByLibraryName(TARGET_LIBRARY_NAME)
                .orElseThrow(() -> new ResourceNotFoundException(TARGET_LIBRARY_NAME + " 정보를 찾을 수 없습니다."));

        LibraryMonthlyStats latestStats = monthlyStatsRepository.findTopByLibraryOrderByStatYearDescStatMonthDesc(library)
                .orElseThrow(() -> new ResourceNotFoundException(TARGET_LIBRARY_NAME + "의 월별 통계 정보를 찾을 수 없습니다."));

        int totalBooks = latestStats.getTotalBooks() != null ? latestStats.getTotalBooks() : 0;
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

    /**
     * 6. [신규] 특정 도서의 최근 12개월 월별 대출 추이 조회
     * [GET] /api/checklists/books/{bookId}/loans/monthly

     * book_snapshots에는 매달 시점의 "누적 대출건수"가 저장돼 있으므로,
     * 이번 달 대출 건수 = 이번 달 누적값 - 지난 달 누적값 (음수가 나오면 0으로 처리 — 카운터 리셋 등 예외 상황 방어).
     */
    public List<BookMonthlyLoanPointDto> getMonthlyLoanTrend(Integer bookId) {
        List<BookSnapshot> snapshots =
                bookSnapshotRepository.findAllByBookBookIdOrderBySnapshotYearAscSnapshotMonthAsc(bookId);

        List<BookMonthlyLoanPointDto> result = new ArrayList<>();
        for (int i = 1; i < snapshots.size(); i++) {
            BookSnapshot prev = snapshots.get(i - 1);
            BookSnapshot curr = snapshots.get(i);

            int prevCount = prev.getCumulativeLoanCount() != null ? prev.getCumulativeLoanCount() : 0;
            int currCount = curr.getCumulativeLoanCount() != null ? curr.getCumulativeLoanCount() : 0;
            int delta = Math.max(0, currCount - prevCount);

            // curr 시점 스냅샷과 prev 시점 스냅샷의 차이(delta)는 "prev월 말 ~ curr월 말" 사이에 일어난 대출 건수다.
            // 즉 이 증가분은 prev월 한 달 동안의 대출량에 가장 가깝다고 보고, curr월이 아닌 prev월로 라벨링한다.
            // (예: 2025.07 스냅샷과 2025.08 스냅샷의 차이 -> "25.07"로 표시)
            String monthLabel = String.format("%02d.%02d", prev.getSnapshotYear() % 100, prev.getSnapshotMonth());

            result.add(BookMonthlyLoanPointDto.builder()
                    .month(monthLabel)
                    .v(delta)
                    .build());
        }

        // 최근 12개월치만 반환 (그보다 스냅샷이 더 쌓여있어도 차트는 12개월 고정)
        if (result.size() > 12) {
            result = result.subList(result.size() - 12, result.size());
        }
        return result;
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

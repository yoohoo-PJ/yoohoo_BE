package com.example.yoohoo_be.checklists.service;

import com.example.yoohoo_be.checklists.domain.Book;
import com.example.yoohoo_be.checklists.domain.BookCheckBatch;
import com.example.yoohoo_be.checklists.domain.BookStatus;
import com.example.yoohoo_be.checklists.dto.BookCheckCompletedListResponseDto;
import com.example.yoohoo_be.checklists.dto.BookDecisionRequestDto;
import com.example.yoohoo_be.checklists.dto.BookDecisionResponseDto;
import com.example.yoohoo_be.checklists.dto.BookSummaryResponseDto;
import com.example.yoohoo_be.checklists.dto.BookWearStatusDetailResponseDto;
import com.example.yoohoo_be.checklists.exception.ResourceNotFoundException;
import com.example.yoohoo_be.checklists.repository.BookCheckBatchRepository;
import com.example.yoohoo_be.checklists.repository.BookRepository;
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
    private final BookRepository bookRepository;

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
    public BookWearStatusDetailResponseDto getWearStatusDetailByBookId(Long bookId) {
        BookCheckBatch batch = bookCheckBatchRepository.findLatestByBookId(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 도서의 점검 이력을 찾을 수 없습니다."));

        var bookInfo = BookWearStatusDetailResponseDto.BookInfoDto.builder()
                .bookId(batch.getBook().getId())
                .title(batch.getBook().getTitle())
                .author(batch.getBook().getAuthor())
                .publisher(batch.getBook().getPublisher())
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
     * 3. 폐기/이관/보존 최종 처리 결정 확정 (bookId 기준 - 기존 버전)
     */
    @Transactional
    public BookDecisionResponseDto decideBookDisposition(Long bookId, BookDecisionRequestDto requestDto) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 도서를 찾을 수 없습니다. id=" + bookId));

        book.decideFinalDisposition(requestDto.getDecision());

        return BookDecisionResponseDto.builder()
                .bookId(book.getId())
                .status(book.getStatus().name())
                .decidedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 4. 특정 상태(예: TRANSFERRED, PRESERVED, DISCARDED)의 도서 목록 조회
     */
    public List<BookSummaryResponseDto> getBooksByStatus(BookStatus status) {
        return bookRepository.findAllByStatus(status)
                .stream()
                .map(book -> BookSummaryResponseDto.builder()
                        .bookId(book.getId())
                        .title(book.getTitle())
                        .author(book.getAuthor())
                        .callNumber(book.getCallNumber())
                        .coverUrl(book.getCoverUrl())
                        .status(book.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    private BookCheckCompletedListResponseDto toCompletedListDto(BookCheckBatch batch) {
        Book book = batch.getBook();
        return BookCheckCompletedListResponseDto.builder()
                .resultBatchId(batch.getId())
                .bookId(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .isbn(book.getIsbn())
                .kdcCode(book.getKdcCode())   // [수정됨] genre -> kdcCode
                .kdcClass(book.getKdcClass()) // [수정됨] turnoverRate -> kdcClass
                .callNumber(book.getCallNumber())
                .coverUrl(book.getCoverUrl())
                .checkedDate(batch.getCheckedDate())
                .librarianName(batch.getLibrarianCode())
                .totalScore(batch.getTotalScore())
                .status(book.getStatus().name())
                .build();
    }
}
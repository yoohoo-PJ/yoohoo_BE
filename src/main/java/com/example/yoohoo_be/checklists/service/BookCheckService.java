package com.example.yoohoo_be.checklists.service;

import com.example.yoohoo_be.checklists.domain.Book;
import com.example.yoohoo_be.checklists.domain.BookCheckBatch;
import com.example.yoohoo_be.checklists.domain.BookCheckResultItem;
import com.example.yoohoo_be.checklists.domain.CheckItem;
import com.example.yoohoo_be.checklists.dto.BookCheckHistoryResponseDto;
import com.example.yoohoo_be.checklists.dto.BookCheckSaveRequestDto;
import com.example.yoohoo_be.checklists.repository.BookCheckBatchRepository;
import com.example.yoohoo_be.checklists.repository.BookRepository;
import com.example.yoohoo_be.checklists.repository.CheckItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookCheckService {

    private final BookRepository bookRepository;
    private final BookCheckBatchRepository bookCheckBatchRepository;
    private final CheckItemRepository checkItemRepository;

    /**
     * 1. 점검 결과 최초 등록 (POST)
     */
    @Transactional
    public Long createBookCheckResult(BookCheckSaveRequestDto requestDto) {
        Book book = bookRepository.findById(requestDto.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("해당 도서를 찾을 수 없습니다. id=" + requestDto.getBookId()));

        BookCheckBatch batch = BookCheckBatch.builder()
                .book(book) // bookId(Long) -> book(Book 연관관계)
                .librarianCode(requestDto.getLibrarianCode())
                .checkedDate(requestDto.getCheckedDate())
                .totalScore(requestDto.getTotalScore())
                .build();

        for (BookCheckSaveRequestDto.CheckItemResultDto itemDto : requestDto.getCheckResults()) {
            CheckItem checkItem = checkItemRepository.findById(itemDto.getCheckItemId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "해당 점검 항목을 찾을 수 없습니다. id=" + itemDto.getCheckItemId()));

            BookCheckResultItem item = BookCheckResultItem.builder()
                    .checkItem(checkItem)
                    .isPassed(itemDto.getIsPassed())
                    .itemScore(itemDto.getItemScore())
                    .note(itemDto.getNote())
                    .build();
            batch.addItem(item);
        }

        BookCheckBatch savedBatch = bookCheckBatchRepository.save(batch);

        // 점검 완료 시 도서 상태를 IN_PROGRESS(마모 처리 현황)로 전환
        boolean isAvailable = requestDto.getTotalScore() >= 60;
        book.completeCheckAndMoveToInProgress("WORN_CHECKED", "SOIL_CHECKED", isAvailable);

        return savedBatch.getId();
    }

    /**
     * 2. 특정 도서의 점검 이력(전체 리스트) 조회 (GET)
     */
    @Transactional(readOnly = true)
    public List<BookCheckHistoryResponseDto> getBookCheckHistory(Long bookId) {
        List<BookCheckBatch> batches = bookCheckBatchRepository.findAllByBookIdOrderByIdDesc(bookId);

        return batches.stream().map(batch -> {
            List<BookCheckHistoryResponseDto.CheckItemDetailDto> itemDtos = batch.getItems().stream()
                    .map(item -> BookCheckHistoryResponseDto.CheckItemDetailDto.builder()
                            .checkItemId(item.getCheckItem().getId())
                            .title(item.getCheckItem().getTitle())
                            .category(item.getCheckItem().getCategory())
                            .description(item.getCheckItem().getDescription())
                            .isPassed(item.getIsPassed())
                            .note(item.getNote())
                            .build())
                    .collect(Collectors.toList());

            return BookCheckHistoryResponseDto.builder()
                    .resultBatchId(batch.getId())
                    .bookId(batch.getBook().getId())
                    .librarianCode(batch.getLibrarianCode())
                    .checkedDate(batch.getCheckedDate())
                    .totalScore(batch.getTotalScore())
                    .items(itemDtos)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 3. 점검 완료 도서 점검리스트 수정 (PUT)
     */
    @Transactional
    public Long updateBookCheckResult(Long resultBatchId, BookCheckSaveRequestDto requestDto) {
        BookCheckBatch batch = bookCheckBatchRepository.findById(resultBatchId)
                .orElseThrow(() -> new IllegalArgumentException("해당 점검 이력을 찾을 수 없습니다. id=" + resultBatchId));

        batch.updateBatch(requestDto.getLibrarianCode(), requestDto.getTotalScore());

        for (BookCheckSaveRequestDto.CheckItemResultDto itemDto : requestDto.getCheckResults()) {
            batch.getItems().stream()
                    .filter(item -> item.getCheckItem().getId().equals(itemDto.getCheckItemId()))
                    .findFirst()
                    .ifPresent(item -> item.updateItemResult(
                            itemDto.getIsPassed(),
                            itemDto.getItemScore(),
                            itemDto.getNote()
                    ));
        }

        return batch.getId();
    }
}
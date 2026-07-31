package com.example.yoohoo_be.checklists.service;

import com.example.yoohoo_be.checklists.domain.Book;
import com.example.yoohoo_be.checklists.domain.BookCheckBatch;
import com.example.yoohoo_be.checklists.domain.BookCheckResultItem;
import com.example.yoohoo_be.checklists.dto.BookCheckHistoryResponseDto;
import com.example.yoohoo_be.checklists.dto.BookCheckSaveRequestDto;
import com.example.yoohoo_be.checklists.repository.BookCheckBatchRepository;
import com.example.yoohoo_be.checklists.repository.BookRepository;
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

    /**
     * 1. 점검 결과 최초 등록 (POST)
     */
    @Transactional
    public Long createBookCheckResult(BookCheckSaveRequestDto requestDto) {
        Book book = bookRepository.findById(requestDto.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("해당 도서를 찾을 수 없습니다. id=" + requestDto.getBookId()));

        BookCheckBatch batch = BookCheckBatch.builder()
                .bookId(requestDto.getBookId())
                .librarianCode(requestDto.getLibrarianCode())
                .checkedDate(requestDto.getCheckedDate())
                .totalScore(requestDto.getTotalScore())
                .build();

        // [수정됨] requestDto.getItems() 로 접근
        for (BookCheckSaveRequestDto.CheckItemResultDto itemDto : requestDto.getItems()) {
            BookCheckResultItem item = BookCheckResultItem.builder()
                    .checkItemId(itemDto.getCheckItemId())
                    .isPassed(itemDto.getIsPassed())
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
     * 2. 특정 도서의 점검 이력 및 상세 조회 (GET)
     */
    @Transactional(readOnly = true)
    public List<BookCheckHistoryResponseDto> getBookCheckHistory(Long bookId) {
        List<BookCheckBatch> batches = bookCheckBatchRepository.findAllByBookIdOrderByIdDesc(bookId);

        return batches.stream().map(batch -> {
            List<BookCheckHistoryResponseDto.CheckItemDetailDto> itemDtos = batch.getItems().stream()
                    .map(item -> BookCheckHistoryResponseDto.CheckItemDetailDto.builder()
                            .checkItemId(item.getCheckItemId())
                            .title("점검 항목 #" + item.getCheckItemId())
                            .category("BOOK_CONDITION") // [추가됨] 명세서 규격 일치
                            .description("도서 마모 및 오염 상태 세부 점검") // [추가됨] 명세서 규격 일치
                            .isPassed(item.getIsPassed())
                            .note(item.getNote())
                            .build())
                    .collect(Collectors.toList());

            return BookCheckHistoryResponseDto.builder()
                    .resultBatchId(batch.getId())
                    .bookId(batch.getBookId())
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

        // [수정됨] requestDto.getItems() 로 변경
        for (BookCheckSaveRequestDto.CheckItemResultDto itemDto : requestDto.getItems()) {
            batch.getItems().stream()
                    .filter(item -> item.getCheckItemId().equals(itemDto.getCheckItemId()))
                    .findFirst()
                    .ifPresent(item -> item.updateItemResult(
                            itemDto.getIsPassed(),
                            null, // 점수는 총점으로 통합 관리되므로 개별 항목 점수는 null 처리
                            itemDto.getNote()
                    ));
        }

        return batch.getId();
    }
}
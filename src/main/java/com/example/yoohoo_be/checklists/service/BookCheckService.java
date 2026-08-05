package com.example.yoohoo_be.checklists.service;

import com.example.yoohoo_be.dashboard.domain.Book;
import com.example.yoohoo_be.dashboard.domain.BookStatus;
import com.example.yoohoo_be.dashboard.domain.UscoreResult;
import com.example.yoohoo_be.checklists.domain.BookCheckBatch;
import com.example.yoohoo_be.checklists.domain.BookCheckResultItem;
import com.example.yoohoo_be.checklists.domain.CheckItem;
import com.example.yoohoo_be.checklists.dto.BookCheckHistoryResponseDto;
import com.example.yoohoo_be.checklists.dto.BookCheckSaveRequestDto;
import com.example.yoohoo_be.checklists.dto.BulkDecisionRequestDto;
import com.example.yoohoo_be.checklists.dto.DecisionConfirmRequestDto;
import com.example.yoohoo_be.checklists.dto.DecisionConfirmResponseDto;
import com.example.yoohoo_be.checklists.exception.InvalidRequestException;
import com.example.yoohoo_be.checklists.repository.BookCheckBatchRepository;
import com.example.yoohoo_be.dashboard.repository.BookRepository;
import com.example.yoohoo_be.dashboard.repository.UscoreResultRepository;
import com.example.yoohoo_be.checklists.repository.CheckItemRepository;
import com.example.yoohoo_be.common.exception.DuplicateResourceException;
import com.example.yoohoo_be.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookCheckService {

    private final BookRepository bookRepository;
    private final BookCheckBatchRepository bookCheckBatchRepository;
    private final CheckItemRepository checkItemRepository;
    private final UscoreResultRepository uscoreResultRepository;

    private static final Map<String, BookStatus> DECISION_TO_STATUS = Map.of(
            "DISPOSAL", BookStatus.DISCARDED,
            "RELOCATION", BookStatus.TRANSFERRED,
            "KEEP", BookStatus.PRESERVED
    );

    /**
     * 1. 점검 결과 최초 등록 (POST)
     */
    @Transactional
    public Long createBookCheckResult(BookCheckSaveRequestDto requestDto) {
        UscoreResult uscore = uscoreResultRepository.findById(requestDto.getResultId().longValue())
                .orElseThrow(() -> new ResourceNotFoundException("해당 유휴도서 결과를 찾을 수 없습니다. resultId=" + requestDto.getResultId()));
        Book book = uscore.getBook();

        if (book.getStatus() == BookStatus.IN_PROGRESS) {
            throw new DuplicateResourceException("이미 등록된 점검 결과입니다.");
        }

        BookCheckBatch batch = BookCheckBatch.builder()
                .book(book) // bookId(Long) -> book(Book 연관관계)
                .librarianCode(requestDto.getLibrarianCode())
                .checkedDate(requestDto.getCheckedDate())
                .totalScore(requestDto.getTotalScore())
                .build();

        for (BookCheckSaveRequestDto.CheckItemResultDto itemDto : requestDto.getCheckResults()) {
            CheckItem checkItem = checkItemRepository.findById(itemDto.getCheckItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "해당 점검 항목을 찾을 수 없습니다. id=" + itemDto.getCheckItemId()));

            BookCheckResultItem item = BookCheckResultItem.builder()
                    .checkItem(checkItem)
                    .isPassed(itemDto.getIsPassed())
                    .itemScore(itemDto.getItemScore())
                    .build();
            batch.addItem(item);
        }

        BookCheckBatch savedBatch = bookCheckBatchRepository.save(batch);

        // 점검 완료 시 도서 상태를 IN_PROGRESS(마모 처리 현황)로 전환
        boolean isAvailable = requestDto.getTotalScore() >= 60;
        book.completeCheckAndMoveToInProgress("WORN_CHECKED", "SOIL_CHECKED", isAvailable);

        // UscoreResult 의 inspectionStatus 도 업데이트 (대기 리스트에서 제외되도록)
        uscoreResultRepository.findByBookBookId(book.getBookId()).ifPresent(uscore -> {
            uscore.updateInspectionStatus(isAvailable ? 
                com.example.yoohoo_be.dashboard.domain.InspectionStatus.PASS : 
                com.example.yoohoo_be.dashboard.domain.InspectionStatus.FAIL);
        });

        return savedBatch.getId();
    }

    /**
     * 2. 특정 도서의 점검 이력(전체 리스트) 조회 (GET)
     */
    @Transactional(readOnly = true)
    public List<BookCheckHistoryResponseDto> getBookCheckHistory(Integer bookId) {
        List<BookCheckBatch> batches = bookCheckBatchRepository.findAllByBookBookIdOrderByIdDesc(bookId);

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
                    .bookId(batch.getBook().getBookId())
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
                            null
                    ));
        }

        return batch.getId();
    }

    /**
     * 4. [신규] 폐기/이관/보존 결정 확정 (resultBatchId 기준)
     * [PUT] /api/checklists/results/{resultBatchId}/decision
     */
    @Transactional
    public DecisionConfirmResponseDto confirmDecision(Long resultBatchId, DecisionConfirmRequestDto requestDto) {
        BookCheckBatch batch = bookCheckBatchRepository.findById(resultBatchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "존재하지 않는 점검 결과입니다. id=" + resultBatchId));

        BookStatus mappedStatus = mapDecisionToBookStatus(requestDto.getDecision());
        batch.getBook().decideFinalDisposition(mappedStatus);

        return DecisionConfirmResponseDto.builder()
                .resultBatchId(batch.getId())
                .decision(requestDto.getDecision())
                .decidedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 5. [신규] 폐기/이관/보존 결정 일괄 확정
     * [PUT] /api/checklists/results/decisions
     */
    @Transactional
    public List<DecisionConfirmResponseDto> confirmBulkDecisions(BulkDecisionRequestDto requestDto) {
        return requestDto.getItems().stream()
                .map(item -> {
                    BookCheckBatch batch = bookCheckBatchRepository.findById(item.getResultBatchId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "존재하지 않는 점검 결과입니다. id=" + item.getResultBatchId()));

                    BookStatus mappedStatus = mapDecisionToBookStatus(item.getDecision());
                    batch.getBook().decideFinalDisposition(mappedStatus);

                    return DecisionConfirmResponseDto.builder()
                            .resultBatchId(batch.getId())
                            .decision(item.getDecision())
                            .decidedAt(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private BookStatus mapDecisionToBookStatus(String decision) {
        BookStatus status = DECISION_TO_STATUS.get(decision);
        if (status == null) {
            throw new InvalidRequestException("decision 값은 DISPOSAL, RELOCATION, KEEP 중 하나여야 합니다.");
        }
        return status;
    }
}
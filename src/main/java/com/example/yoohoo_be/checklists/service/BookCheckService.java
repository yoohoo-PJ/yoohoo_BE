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
import com.example.yoohoo_be.checklists.dto.DiscardQuotaDto;
import com.example.yoohoo_be.checklists.exception.InvalidRequestException;
import com.example.yoohoo_be.checklists.repository.BookCheckBatchRepository;
import com.example.yoohoo_be.checklists.repository.CheckItemRepository;
import com.example.yoohoo_be.dashboard.repository.BookRepository;
import com.example.yoohoo_be.dashboard.repository.UscoreResultRepository;
import com.example.yoohoo_be.common.exception.DuplicateResourceException;
import com.example.yoohoo_be.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookCheckService {

    private final BookRepository bookRepository;
    private final BookCheckBatchRepository bookCheckBatchRepository;
    private final CheckItemRepository checkItemRepository;
    private final UscoreResultRepository uscoreResultRepository;
    private final BookWearStatusService bookWearStatusService;
    private final IllRecommendationService illRecommendationService;

    private static final Map<String, BookStatus> DECISION_TO_STATUS = Map.of(
            "DISPOSAL", BookStatus.DISCARDED,
            "RELOCATION", BookStatus.TRANSFERRED,
            "KEEP", BookStatus.PRESERVED
    );

    /**
     * 1. 점검 결과 최초 등록 (POST)
     */
    @Transactional
    public BookCheckBatch createBookCheckResult(BookCheckSaveRequestDto requestDto) {
        if (requestDto.getResultId() == null) {
            throw new InvalidRequestException("점검 대상 도서 ID(resultId)는 필수 값입니다.");
        }
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
        uscore.updateInspectionStatus(isAvailable ?
                com.example.yoohoo_be.dashboard.domain.InspectionStatus.PASS :
                com.example.yoohoo_be.dashboard.domain.InspectionStatus.FAIL);

        return savedBatch;
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

        if (mappedStatus == BookStatus.DISCARDED) {
            long currentDiscarded = bookRepository.countByStatus(BookStatus.DISCARDED);
            validateDiscardQuota(currentDiscarded + 1);
        }

        // 이관(RELOCATION) 결정 시 상호대차 추천 알고리즘 먼저 실행
        // (X-Lock 점유 전 실행하여 REQUIRES_NEW 내부 트랜잭션과의 DB 데드락 방지)
        if (mappedStatus == BookStatus.TRANSFERRED) {
            try {
                illRecommendationService.generateRecommendations(batch.getBook());
            } catch (Exception e) {
                log.error("⚠ 상호대차 추천 알고리즘 실행 실패 (bookId={}): {}",
                        batch.getBook().getBookId(), e.getMessage(), e);
            }
        }

        // 마지막에 상태 업데이트 (이 시점에 Book 테이블 레코드에 X-Lock이 걸림)
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
     *
     * 일괄 확정 중 DISPOSAL(폐기) 건은 처리 순서대로 누적 카운트하며 매 건마다
     * 연간 폐기 상한(전체 장서의 7%)을 검증한다. 상한을 넘는 시점부터는 요청 전체를
     * 거부하여, 상한 초과 건이 일부만 저장되는 일이 없도록 한다.
     */
    @Transactional
    public List<DecisionConfirmResponseDto> confirmBulkDecisions(BulkDecisionRequestDto requestDto) {
        long[] runningDiscardedCount = { bookRepository.countByStatus(BookStatus.DISCARDED) };

        return requestDto.getItems().stream()
                .map(item -> {
                    BookCheckBatch batch = bookCheckBatchRepository.findById(item.getResultBatchId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "존재하지 않는 점검 결과입니다. id=" + item.getResultBatchId()));

                    BookStatus mappedStatus = mapDecisionToBookStatus(item.getDecision());

                    if (mappedStatus == BookStatus.DISCARDED) {
                        runningDiscardedCount[0]++;
                        validateDiscardQuota(runningDiscardedCount[0]);
                    }

                    // 이관(RELOCATION) 결정 시 상호대차 추천 알고리즘 먼저 실행
                    // (X-Lock 점유 전 실행하여 REQUIRES_NEW 내부 트랜잭션과의 DB 데드락 방지)
                    if (mappedStatus == BookStatus.TRANSFERRED) {
                        try {
                            illRecommendationService.generateRecommendations(batch.getBook());
                        } catch (Exception e) {
                            log.error("⚠ 일괄 이관 추천 알고리즘 실행 실패 (bookId={}): {}",
                                    batch.getBook().getBookId(), e.getMessage(), e);
                        }
                    }

                    // 마지막에 상태 업데이트
                    batch.getBook().decideFinalDisposition(mappedStatus);

                    return DecisionConfirmResponseDto.builder()
                            .resultBatchId(batch.getId())
                            .decision(item.getDecision())
                            .decidedAt(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 도서관법 시행령 [별표 7] 제3호에 따른 연간 폐기 상한(전체 장서 × 7%) 검증.
     * projectedDiscardedCount(이번 건 포함 예상 폐기 누적 건수)가 상한을 넘으면 처리를 거부한다.
     * 전체 장서 수 기준은 BookWearStatusService.calculateDiscardQuota()와 동일하게
     * 개요 화면 "소장 도서 수"(LibraryMonthlyStats 최신 월)를 그대로 재사용한다.
     */
    private void validateDiscardQuota(long projectedDiscardedCount) {
        DiscardQuotaDto quota = bookWearStatusService.calculateDiscardQuota();

        if (projectedDiscardedCount > quota.getCapCount()) {
            throw new InvalidRequestException(String.format(
                    "연간 폐기 상한을 초과하여 처리할 수 없습니다. (도서관법 시행령 [별표7] 제3호: 전체 장서의 100분의 7 이내) " +
                            "전체 장서 %d권 기준 상한 %d권이며, 이번 처리 시 누적 폐기 건수는 %d권이 됩니다.",
                    quota.getTotalBooks(), quota.getCapCount(), projectedDiscardedCount));
        }
    }

    private BookStatus mapDecisionToBookStatus(String decision) {
        BookStatus status = DECISION_TO_STATUS.get(decision);
        if (status == null) {
            throw new InvalidRequestException("decision 값은 DISPOSAL, RELOCATION, KEEP 중 하나여야 합니다.");
        }
        return status;
    }
}
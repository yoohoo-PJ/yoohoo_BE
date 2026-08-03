package com.example.yoohoo_be.checklists.controller;

import com.example.yoohoo_be.checklists.dto.BookCheckHistoryResponseDto;
import com.example.yoohoo_be.checklists.dto.BookCheckSaveRequestDto;
import com.example.yoohoo_be.checklists.dto.BulkDecisionRequestDto;
import com.example.yoohoo_be.checklists.dto.DecisionConfirmRequestDto;
import com.example.yoohoo_be.checklists.dto.DecisionConfirmResponseDto;
import com.example.yoohoo_be.checklists.service.BookCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checklists")
@RequiredArgsConstructor
public class BookCheckController {

    private final BookCheckService bookCheckService;

    /**
     * 1. 개별 도서 점검 결과 일괄 등록
     * [POST] /api/checklists/results
     */
    @PostMapping("/results")
    public ResponseEntity<Map<String, Object>> registerCheckResult(
            @Valid @RequestBody BookCheckSaveRequestDto requestDto) {

        Long resultBatchId = bookCheckService.createBookCheckResult(requestDto);

        Map<String, Object> data = new HashMap<>();
        data.put("resultBatchId", resultBatchId);
        data.put("totalScore", requestDto.getTotalScore());
        data.put("checkedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "도서 점검 결과가 성공적으로 저장되었습니다.");
        response.put("data", data);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 2. 특정 도서의 점검 이력(전체 리스트) 조회
     * [GET] /api/checklists/books/{bookId}/results/history
     * BookWearStatusController 의 상세(단건) 조회와 경로가 겹쳐서 분리함
     * (상세 모달: /books/{bookId}/results, 전체 이력: /books/{bookId}/results/history)
     */
    @GetMapping("/books/{bookId}/results/history")
    public ResponseEntity<List<BookCheckHistoryResponseDto>> getBookCheckHistory(@PathVariable("bookId") Integer bookId) {
        List<BookCheckHistoryResponseDto> history = bookCheckService.getBookCheckHistory(bookId);
        return ResponseEntity.ok(history);
    }

    /**
     * 3. [마모 처리 현황] 점검 완료 도서 점검리스트 수정
     * [PUT] /api/checklists/results/{resultBatchId}
     */
    @PutMapping("/results/{resultBatchId}")
    public ResponseEntity<Map<String, Object>> updateCheckResult(
            @PathVariable("resultBatchId") Long resultBatchId,
            @Valid @RequestBody BookCheckSaveRequestDto requestDto) {

        Long updatedBatchId = bookCheckService.updateBookCheckResult(resultBatchId, requestDto);

        Map<String, Object> data = new HashMap<>();
        data.put("resultBatchId", updatedBatchId);
        data.put("totalScore", requestDto.getTotalScore());
        data.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "점검 결과가 성공적으로 수정되었습니다.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    /**
     * 4. 폐기/이관/보존 결정 확정 (resultBatchId 기준)
     * [PUT] /api/checklists/results/{resultBatchId}/decision
     */
    @PutMapping("/results/{resultBatchId}/decision")
    public ResponseEntity<Map<String, Object>> confirmDecision(
            @PathVariable Long resultBatchId,
            @Valid @RequestBody DecisionConfirmRequestDto requestDto) {

        DecisionConfirmResponseDto data = bookCheckService.confirmDecision(resultBatchId, requestDto);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "도서 처리 결정이 성공적으로 확정되었습니다.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    /**
     * 5. 폐기/이관/보존 결정 일괄 확정
     * [PUT] /api/checklists/results/decisions
     */
    @PutMapping("/results/decisions")
    public ResponseEntity<Map<String, Object>> confirmBulkDecisions(
            @Valid @RequestBody BulkDecisionRequestDto requestDto) {

        List<DecisionConfirmResponseDto> data = bookCheckService.confirmBulkDecisions(requestDto);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", data.size() + "건의 도서 처리 결정이 성공적으로 확정되었습니다.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
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
     * 응답: 201(등록 성공) / 400(필수 필드 누락 등) / 401(인증 만료) / 404(존재하지 않는 bookId)
     * - 400, 404 는 GlobalExceptionHandler 가 처리 (BookCheckService 에서
     *   bookId/checkItemId 못 찾으면 ResourceNotFoundException -> 404,
     *   @Valid 검증 실패는 MethodArgumentNotValidException -> 400)
     * - 401 은 인증 붙을 때 GlobalExceptionHandler.handleAuthentication 이 처리
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

        // [수정됨] 200 -> 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 2. 특정 도서의 점검 이력(전체 리스트) 조회
     * [GET] /api/checklists/books/{bookId}/results/history
     */
    @GetMapping("/books/{bookId}/results/history")
    public ResponseEntity<Map<String, Object>> getCheckHistory(
            @PathVariable Long bookId) {

        List<BookCheckHistoryResponseDto> history = bookCheckService.getBookCheckHistory(bookId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "점검 이력을 성공적으로 조회했습니다.");
        response.put("data", history);

        return ResponseEntity.ok(response);
    }

    /**
     * 3. [마모 처리 현황] 점검 완료 도서 점검리스트 수정
     * [PUT] /api/checklists/results/{resultBatchId}
     */
    @PutMapping("/results/{resultBatchId}")
    public ResponseEntity<Map<String, Object>> updateCheckResult(
            @PathVariable Long resultBatchId,
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
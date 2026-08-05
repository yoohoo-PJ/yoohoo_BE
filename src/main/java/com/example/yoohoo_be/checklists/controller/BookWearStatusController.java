package com.example.yoohoo_be.checklists.controller;

import com.example.yoohoo_be.dashboard.domain.BookStatus;
import com.example.yoohoo_be.checklists.dto.BookCheckCompletedListResponseDto;
import com.example.yoohoo_be.checklists.dto.BookSummaryResponseDto;
import com.example.yoohoo_be.checklists.dto.BookWearStatusDetailResponseDto;
import com.example.yoohoo_be.checklists.dto.DiscardedBookListResponseDto;
import com.example.yoohoo_be.checklists.service.BookWearStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checklists")
@RequiredArgsConstructor
public class BookWearStatusController {

    private final BookWearStatusService bookWearStatusService;

    /**
     * 1. 점검 완료 도서(마모 처리 현황) 목록 조회 API
     * [GET] /api/checklists/results/completed
     */
    @GetMapping("/results/completed")
    public ResponseEntity<List<BookCheckCompletedListResponseDto>> getCompletedBookCheckList() {
        List<BookCheckCompletedListResponseDto> data = bookWearStatusService.getCompletedBookChecks();
        return ResponseEntity.ok(data);
    }

    /**
     * 2. 특정 도서의 마모 상태 점검 상세 결과 조회 API (도서 클릭 시 뜨는 상세 모달)
     * [GET] /api/checklists/books/{bookId}/results
     */
    @GetMapping("/books/{bookId}/results")
    public ResponseEntity<BookWearStatusDetailResponseDto> getWearStatusDetail(@PathVariable("bookId") Integer bookId) {
        BookWearStatusDetailResponseDto data = bookWearStatusService.getWearStatusDetailByBookId(bookId);
        return ResponseEntity.ok(data);
    }


    /**
     * 4. [추가됨] 특정 상태(DISCARDED/TRANSFERRED/PRESERVED 등)의 도서 목록 조회
     * [GET] /api/checklists/books?status=TRANSFERRED
     */
    @GetMapping("/books")
    public ResponseEntity<Map<String, Object>> getBooksByStatus(
            @RequestParam("status") BookStatus status) {

        List<BookSummaryResponseDto> data = bookWearStatusService.getBooksByStatus(status);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", status.name() + " 도서 목록을 조회했습니다.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    /**
     * 5. [신규] 폐기 도서 목록 화면 (5번째 메뉴)
     * 폐기 확정 도서 목록과 함께, 도서관법 시행령 [별표7] 제3호에 따른
     * 연간 폐기 상한(전체 장서의 100분의 7) 현황을 함께 제공한다.
     * [GET] /api/checklists/discarded
     */
    @GetMapping("/discarded")
    public ResponseEntity<Map<String, Object>> getDiscardedBooks() {
        DiscardedBookListResponseDto data = bookWearStatusService.getDiscardedBooksWithQuota();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "폐기 도서 목록 및 연간 폐기 상한 현황을 조회했습니다.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
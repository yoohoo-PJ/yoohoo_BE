package com.example.yoohoo_be.checklists.controller;

import com.example.yoohoo_be.checklists.dto.BookCheckCompletedListResponseDto;
import com.example.yoohoo_be.checklists.dto.BookWearStatusDetailResponseDto;
import com.example.yoohoo_be.checklists.service.BookWearStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<BookWearStatusDetailResponseDto> getBookWearStatusDetail(
            @PathVariable Long bookId) {
        BookWearStatusDetailResponseDto data = bookWearStatusService.getWearStatusDetailByBookId(bookId);
        return ResponseEntity.ok(data);
    }
}
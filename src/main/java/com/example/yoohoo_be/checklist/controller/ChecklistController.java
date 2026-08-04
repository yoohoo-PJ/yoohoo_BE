package com.example.yoohoo_be.checklist.controller;

import com.example.yoohoo_be.checklist.dto.DamagePendingListDto;
import com.example.yoohoo_be.checklist.service.ChecklistService;
import com.example.yoohoo_be.dashboard.dto.PaginatedApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.yoohoo_be.checklist.dto.AlgorithmRunResponseDto;

@RestController
@RequestMapping("/api/checklists")
@RequiredArgsConstructor
public class ChecklistController {

    private final ChecklistService checklistService;

    @GetMapping
    public ResponseEntity<?> getChecklists(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "genre", required = false) String genre,
            @RequestParam(name = "sortOrder", required = false) String sortOrder,
            Pageable pageable) {
        
        if ("DAMAGE_PENDING".equals(status)) {
            Page<DamagePendingListDto> pageData = checklistService.getDamagePendingPage(keyword, genre, sortOrder, pageable);
            return ResponseEntity.ok(new PaginatedApiResponse<>(true, pageData));
        }
        
        throw new IllegalArgumentException("Unsupported status or missing status parameter");
    }

    // 알고리즘 수동 실행 시뮬레이션
    @PostMapping("/idle-classify")
    public ResponseEntity<com.example.yoohoo_be.dashboard.dto.ApiResponse<AlgorithmRunResponseDto>> runIdleClassify() {
        AlgorithmRunResponseDto result = checklistService.runIdleClassificationAlgorithm();
        return ResponseEntity.ok(new com.example.yoohoo_be.dashboard.dto.ApiResponse<>(true, result));
    }
}

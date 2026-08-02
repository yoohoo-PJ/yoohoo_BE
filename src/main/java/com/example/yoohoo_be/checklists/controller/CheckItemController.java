package com.example.yoohoo_be.checklists.controller;

import com.example.yoohoo_be.checklists.dto.CheckItemRequestDto;
import com.example.yoohoo_be.checklists.dto.CheckItemResponseDto;
import com.example.yoohoo_be.checklists.service.CheckItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 점검 항목(CheckItem) 마스터 데이터 관리용 API.
 * BookCheckService 가 checkItemId 로 실제 CheckItem 을 조회하도록
 * 되어 있어서, 테스트/운영을 위해 최소한의 등록·조회 기능을 제공한다.
 */
@RestController
@RequestMapping("/api/checklists/check-items")
@RequiredArgsConstructor
public class CheckItemController {

    private final CheckItemService checkItemService;

    /**
     * 점검 항목 등록
     * [POST] /api/checklists/check-items
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCheckItem(
            @Valid @RequestBody CheckItemRequestDto requestDto) {

        Long checkItemId = checkItemService.createCheckItem(requestDto);

        Map<String, Object> data = new HashMap<>();
        data.put("checkItemId", checkItemId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "점검 항목이 성공적으로 등록되었습니다.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    /**
     * 점검 항목 전체 목록 조회
     * [GET] /api/checklists/check-items
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCheckItems() {
        List<CheckItemResponseDto> items = checkItemService.getAllCheckItems();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "점검 항목 목록을 성공적으로 조회했습니다.");
        response.put("data", items);

        return ResponseEntity.ok(response);
    }
}
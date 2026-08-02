package com.example.yoohoo_be.transfer.controller;

import com.example.yoohoo_be.dashboard.domain.TransferStatus;
import com.example.yoohoo_be.transfer.dto.TransferPageResponseDto;
import com.example.yoohoo_be.transfer.dto.TransferStatusUpdateRequestDto;
import com.example.yoohoo_be.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @GetMapping
    public ResponseEntity<TransferPageResponseDto> getTransfers(
            @RequestParam(name = "status", defaultValue = "PENDING,IN_TRANSIT") java.util.List<TransferStatus> statuses,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        
        // TODO: 현재 로그인한 사용자의 도서관 정보 추출 (보안 적용 시)
        // 임시로 경기도교육청중앙도서관으로 하드코딩
        String myLibraryName = "경기도교육청중앙도서관"; 

        TransferPageResponseDto response = transferService.getTransferList(statuses, pageable, myLibraryName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{recommendationId}/execute")
    public ResponseEntity<Void> executeTransfer(
            @PathVariable(name = "recommendationId") Long recommendationId) {
        
        transferService.executeTransfer(recommendationId);
        return ResponseEntity.ok().build();
    }
}

package com.example.yoohoo_be.checklists.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookDecisionResponseDto {
    private Long bookId;
    private String status;          // 확정된 최종 상태 (DISCARDED / TRANSFERRED / PRESERVED)
    private LocalDateTime decidedAt; // 확정 처리 시각
}
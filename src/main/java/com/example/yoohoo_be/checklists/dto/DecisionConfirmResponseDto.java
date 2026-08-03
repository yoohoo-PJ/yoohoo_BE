package com.example.yoohoo_be.checklists.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DecisionConfirmResponseDto {

    private Long resultBatchId;
    private String decision; // 확정된 결정값 (DISPOSAL/RELOCATION/KEEP, 요청값 그대로 echo)
    private LocalDateTime decidedAt; // 서버 기준 확정 시각
}

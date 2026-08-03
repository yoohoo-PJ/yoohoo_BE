package com.example.yoohoo_be.checklists.dto;

import com.example.yoohoo_be.dashboard.domain.BookStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookDecisionRequestDto {

    @NotNull(message = "처리 결정 값은 필수입니다. (DISCARDED, TRANSFERRED, PRESERVED 중 하나)")
    private BookStatus decision; // 최종 처리 결정 (DISCARDED / TRANSFERRED / PRESERVED)

    private String reason; // 결정 사유 (선택, ex: "파손 심각으로 이관 처리")
}
package com.example.yoohoo_be.checklists.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class BulkDecisionRequestDto {

    @NotBlank(message = "librarianCode는 필수입니다.")
    private String librarianCode;

    @NotBlank(message = "decidedDate는 필수입니다.")
    private String decidedDate;

    @Valid
    @NotEmpty(message = "items는 1건 이상이어야 합니다.")
    private List<DecisionItemDto> items;

    @Getter
    @NoArgsConstructor
    public static class DecisionItemDto {

        @NotNull(message = "resultBatchId는 필수입니다.")
        private Long resultBatchId;

        @NotBlank(message = "decision 값은 필수입니다.")
        private String decision;
    }
}

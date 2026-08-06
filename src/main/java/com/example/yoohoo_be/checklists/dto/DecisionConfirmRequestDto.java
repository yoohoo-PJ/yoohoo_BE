package com.example.yoohoo_be.checklists.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DecisionConfirmRequestDto {

    @NotBlank(message = "decision 값은 필수입니다.")
    private String decision;
}

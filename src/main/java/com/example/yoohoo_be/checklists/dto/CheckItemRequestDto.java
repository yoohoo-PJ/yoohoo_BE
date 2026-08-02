package com.example.yoohoo_be.checklists.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CheckItemRequestDto {

    @NotBlank(message = "점검 항목 제목은 필수 항목입니다.")
    private String title; // 점검 항목 제목 (예: "표지 찢어짐")

    private String category; // 점검 카테고리 (예: BOOK, COVER 등)

    private String description; // 점검 상세 설명

    private Integer maxScore; // 배점(만점) - 참고용
}
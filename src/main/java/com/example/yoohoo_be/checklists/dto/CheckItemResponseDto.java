package com.example.yoohoo_be.checklists.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckItemResponseDto {
    private Long id;
    private String title;
    private String category;
    private String description;
    private Integer maxScore;
}
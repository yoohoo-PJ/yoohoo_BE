package com.example.yoohoo_be.checklists.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookSummaryResponseDto {
    private Long bookId;
    private String title;
    private String author;
    private String callNumber;
    private String coverUrl;
    private String status; // DISCARDED / TRANSFERRED / PRESERVED 등
}
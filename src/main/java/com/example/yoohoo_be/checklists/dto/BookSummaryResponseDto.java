package com.example.yoohoo_be.checklists.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookSummaryResponseDto {
    private Integer bookId;
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private String kdcCode;
    private String kdcClass;
    private String callNumber;
    private String coverUrl;
    private String status; // DISCARDED / TRANSFERRED / PRESERVED 등
}
package com.example.yoohoo_be.checklist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DamagePendingListDto {
    private Long resultId;
    private String bookTitle;
    private String author;
    private String genre;
    private String isbn;
    private Double idleScore;
    
    // 세부 점수
    private Double sAge;
    private Double sLoan;
    private Double sDecay;
}

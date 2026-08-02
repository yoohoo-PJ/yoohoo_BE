package com.example.yoohoo_be.checklist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AlgorithmRunResponseDto {
    private String libraryName;
    private long totalAnalyzedBooks;
    private long newlyClassifiedIdleBooks;
    private long executionTimeMs;
    private java.util.List<IdleBookDto> idleBooks; // 유휴화 판정된 책 목록

    @Getter
    @Builder
    @AllArgsConstructor
    public static class IdleBookDto {
        private String bookTitle;
        private String author;
        private Double uScore;
    }
}

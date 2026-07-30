package com.example.yoohoo_be.checklists.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BookCheckHistoryResponseDto {

    private Long resultBatchId;  // 점검 제출 그룹 고유 ID
    private Long bookId;         // 도서 고유 ID
    private String librarianCode;// 점검 수행 담당자 코드
    private String checkedDate;  // 점검 날짜 (YYYY-MM-DD)
    private Integer totalScore;  // 해당 점검 건의 최종 총 점수
    private List<CheckItemDetailDto> items; // 상세 점검 항목 리스트

    @Getter
    @Builder
    public static class CheckItemDetailDto {
        private Long checkItemId;  // 점검 항목 ID
        private String title;      // 점검 항목 제목
        private String category;   // 점검 카테고리 (예: BOOK, COVER 등)
        private String description;// 점검 상세 설명
        private Boolean isPassed;  // 통과 여부 (true: 통과, false: 불량)
        private String note;       // 특이사항 메모
    }
}
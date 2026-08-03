package com.example.yoohoo_be.checklists.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCheckCompletedListResponseDto {

    private Long resultBatchId;
    private Long bookId;
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private String kdcCode;    // [수정됨] genre -> kdcCode (실제 DB 필드로 교체)
    private String kdcClass;   // [수정됨] turnoverRate -> kdcClass (실제 DB 필드로 교체)
    private String callNumber;
    private String coverUrl;
    private String checkedDate;
    private String librarianName;
    private Integer totalScore;
    private String status;
}
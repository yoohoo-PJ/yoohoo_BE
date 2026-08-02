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

    private Long resultBatchId;   // 점검 제출 그룹 ID (BookCheckBatch PK)
    private Long bookId;          // 도서 고유 ID
    private String title;         // 도서 제목
    private String author;        // 저자
    private String publisher;     // 출판사
    private String genre;         // 도서 장르
    private String isbn;          // 도서 ISBN
    private String callNumber;    // 청구기호
    private String coverUrl;      // 도서 표지 이미지 URL
    private Double turnoverRate;  // 도서 회전율 (값이 없으면 null)
    private String checkedDate;   // 점검 날짜 (YYYY-MM-DD)
    private String librarianName; // 담당 사서 코드(또는 이름)
    private Integer totalScore;   // 총점
    private String status;        // 도서 상태
}
 

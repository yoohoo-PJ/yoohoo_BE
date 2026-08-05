package com.example.yoohoo_be.checklists.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookWearStatusDetailResponseDto {

    private Long resultBatchId;
    private String checkedDate;
    private String librarianCode;
    private Integer totalScore;

    private BookInfoDto bookInfo;

    private List<CheckResultItemDto> checkResults;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookInfoDto {
        private Integer bookId;
        private String title;
        private String author;
        private String publisher;
        private String isbn;
        private String kdcCode;
        private String kdcClass;
        private String genre;
        private String callNumber;
        private String coverUrl;
        private String status;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckResultItemDto {
        private Long checkItemId;
        private String title;
        
        @JsonProperty("isPassed")
        private Boolean isPassed;
        
        private Integer itemScore;
        private String note;
    }
}
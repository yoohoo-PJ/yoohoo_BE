package com.example.yoohoo_be.checklists.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class BookCheckSaveRequestDto {

    // 등록(POST)에서만 필요한 값 (UscoreResult 조회용). 수정(PUT)은 resultBatchId로 대상을 특정하므로
    // 이 값을 쓰지 않는다 — 그래서 등록/수정 공용 DTO인 이 클래스에는 @NotNull을 걸지 않고,
    // 실제로 필요한 BookCheckService#createBookCheckResult에서만 null 체크한다.
    private Integer resultId; // 점검 대상 UscoreResult ID (구 필드명은 bookId였음)

    private Integer bookId; // 새로 추가된 실제 bookId

    @NotBlank(message = "사서/담당자 코드는 필수 항목입니다.")
    private String librarianCode; // 담당 사서 코드

    @NotBlank(message = "점검 날짜(YYYY-MM-DD)는 필수 항목입니다.")
    private String checkedDate; // 점검 날짜 (YYYY-MM-DD)

    @NotNull(message = "총 점수는 필수 항목입니다.")
    private Integer totalScore; // 현재 설정된 점검리스트 총 점수

    // API 명세서 필드명(checkResults)과 일치시킴
    @Valid
    @NotNull(message = "상세 점검 항목 리스트는 필수 항목입니다.")
    @jakarta.validation.constraints.Size(min = 15, max = 15, message = "점검 항목은 정확히 15개여야 합니다.")
    private List<CheckItemResultDto> checkResults;

    @Getter
    @NoArgsConstructor
    public static class CheckItemResultDto {

        @NotNull(message = "점검 항목 ID는 필수 항목입니다.")
        private Long checkItemId; // 점검 항목 ID

        @NotNull(message = "통과 여부는 필수 항목입니다.")
        @JsonProperty("isPassed")
        private Boolean isPassed; // 통과 여부

        private Integer itemScore; // 해당 항목 판정 점수
    }
}
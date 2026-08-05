package com.example.yoohoo_be.checklists.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * [폐기 도서 목록] 화면 응답 - 폐기 확정 도서 목록 + 연간 폐기 상한(7%) 현황을 함께 제공한다.
 */
@Getter
@Builder
public class DiscardedBookListResponseDto {
    private List<BookSummaryResponseDto> books;
    private DiscardQuotaDto quota;
}

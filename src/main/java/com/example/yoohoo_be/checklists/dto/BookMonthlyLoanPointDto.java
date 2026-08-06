package com.example.yoohoo_be.checklists.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * [유휴 도서 처리 목록] 상세 패널의 "최근 12개월 월별 대출 추이" 차트 한 점(한 달)에 대응.
 */
@Getter
@Builder
public class BookMonthlyLoanPointDto {
    private String month;
    private int v;
}

package com.example.yoohoo_be.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyLoanDto {
    private String yearMonth;
    private Integer totalBooks;
    private Integer totalLoans;
    private Double turnoverRate;
}

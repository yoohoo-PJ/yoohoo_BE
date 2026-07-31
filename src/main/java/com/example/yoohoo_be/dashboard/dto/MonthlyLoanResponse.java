package com.example.yoohoo_be.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MonthlyLoanResponse {
    
    private String date;
    private Integer totalBooks;
    private Integer totalLoans;
    private Double turnoverRate;

}

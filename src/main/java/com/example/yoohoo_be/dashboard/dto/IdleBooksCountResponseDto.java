package com.example.yoohoo_be.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdleBooksCountResponseDto {
    private long currentMonthCount;
    private long lastMonthCount;
    private int percentageChange;
}

package com.example.yoohoo_be.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserDistributionResponse {
    
    private List<RegionData> distributions;

    @Getter
    @AllArgsConstructor
    public static class RegionData {
        private String region;      // 지역명 (예: 장안구, 권선구 등)
        private Integer userCount;  // 해당 지역 이용자 수
        private Double percentage;  // 전체 대비 비율 (%)
    }
}

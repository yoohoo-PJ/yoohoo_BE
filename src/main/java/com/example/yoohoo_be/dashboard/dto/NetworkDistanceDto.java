package com.example.yoohoo_be.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkDistanceDto {
    private String libraryName;
    private String address;
    private Integer bookCount;
    private Double length; // 카카오 API로 계산된 최단 거리 (km)
}

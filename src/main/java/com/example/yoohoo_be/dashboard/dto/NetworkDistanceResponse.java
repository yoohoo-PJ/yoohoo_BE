package com.example.yoohoo_be.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NetworkDistanceResponse {
    
    private List<DistanceInfo> distances;

    @Getter
    @AllArgsConstructor
    public static class DistanceInfo {
        private String lib;
        private Double distanceKm;
    }
}

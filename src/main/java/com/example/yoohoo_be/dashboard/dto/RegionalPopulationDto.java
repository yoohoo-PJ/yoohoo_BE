package com.example.yoohoo_be.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegionalPopulationDto {
    private String districtName;
    private Integer totalPopulation;
    private AgeDistributionDto ageDistribution;

    @Getter
    @Builder
    public static class AgeDistributionDto {
        private AgeGroupDto ageUnder10;
        private AgeGroupDto age10s;
        private AgeGroupDto age20s;
        private AgeGroupDto age30s;
        private AgeGroupDto age40s;
        private AgeGroupDto age50s;
        private AgeGroupDto age60Plus;
    }

    @Getter
    @AllArgsConstructor
    public static class AgeGroupDto {
        private double percentage;
        private Integer count;
    }
}
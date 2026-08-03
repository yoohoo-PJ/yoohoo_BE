package com.example.yoohoo_be.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class TransferResponseDto {
    private Long recommendationId;
    private String bookTitle;
    private String genre;
    private String originLibrary;
    private String destLibrary;
    private BigDecimal distanceKm;
    private BigDecimal matchingScore;
    private String direction;
    private String status;
    private ScoreDetails scoreDetails;
    private List<TransferAlternativeDto> alternatives;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ScoreDetails {
        private BigDecimal distanceDecay;
        private BigDecimal bookDemand;
        private BigDecimal shortageResolution;
        private BigDecimal spaceEfficiency;
    }
}

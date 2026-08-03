package com.example.yoohoo_be.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class TransferAlternativeDto {
    private Long recommendationId;
    private String originLibrary;
    private String destLibrary;
    private BigDecimal distanceKm;
    private BigDecimal matchingScore;
    private String direction;
    private String status;
}

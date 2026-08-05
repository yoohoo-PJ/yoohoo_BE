package com.example.yoohoo_be.checklists.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 도서관법 시행령 [별표 7] 제3호(도서관자료의 폐기 및 제적의 범위)에 근거한
 * 연간 폐기 상한(전체 장서의 100분의 7) 현황 정보.
 */
@Getter
@Builder
public class DiscardQuotaDto {
    private int totalBooks;          // 전체 장서 수
    private long discardedCount;     // 현재까지 폐기 확정된 도서 수
    private long capCount;           // 상한 건수 (전체 장서 × 7%, 소수점 이하 절사)
    private double capRatio;         // 법정 상한 비율 (0.07)
    private long remaining;          // 남은 처리 가능 건수 (0 이상)
    private boolean capReached;      // 상한 도달 여부
}

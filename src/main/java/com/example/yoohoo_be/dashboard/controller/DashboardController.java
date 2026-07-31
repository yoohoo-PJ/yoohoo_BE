package com.example.yoohoo_be.dashboard.controller;

import com.example.yoohoo_be.dashboard.dto.ApiResponse;
import com.example.yoohoo_be.dashboard.dto.MonthlyLoanDto;
import com.example.yoohoo_be.dashboard.dto.NetworkDistanceDto;
import com.example.yoohoo_be.dashboard.dto.RegionalPopulationDto;
import com.example.yoohoo_be.dashboard.service.DashboardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 지역별 인원 분포 목록 조회
    @GetMapping("/users/distribution")
    public ApiResponse<RegionalPopulationDto> getUserDistribution() {
        RegionalPopulationDto distributionData = dashboardService.getUserDistribution();
        return new ApiResponse<>(true, distributionData);
    }

    // 도서관 네트워크 거리 조회
    @GetMapping("/libraries/network-distances")
    public ApiResponse<List<NetworkDistanceDto>> getNetworkDistances() {
        List<NetworkDistanceDto> distances = dashboardService.getRealDistancesFromBuksuwon();
        return new ApiResponse<>(true, distances);
    }

    // 도서관 월별 대출 현황 조회 (12개월)
    @GetMapping("/loans/monthly")
    public ApiResponse<List<MonthlyLoanDto>> getMonthlyLoans() {
        List<MonthlyLoanDto> monthlyLoans = dashboardService.getMonthlyLoans();
        return new ApiResponse<>(true, monthlyLoans);
    }
}
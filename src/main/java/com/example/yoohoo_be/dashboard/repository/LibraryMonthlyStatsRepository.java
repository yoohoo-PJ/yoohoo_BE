package com.example.yoohoo_be.dashboard.repository;

import com.example.yoohoo_be.dashboard.domain.Library;
import com.example.yoohoo_be.dashboard.domain.LibraryMonthlyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibraryMonthlyStatsRepository extends JpaRepository<LibraryMonthlyStats, Integer> {
    
    // 특정 도서관의 통계를 연도와 월의 내림차순(최신순)으로 정렬하여 상위 12개만 가져옴
    List<LibraryMonthlyStats> findTop12ByLibraryOrderByStatYearDescStatMonthDesc(Library library);

    // 특정 도서관의 특정 연월 통계 가져오기
    java.util.Optional<LibraryMonthlyStats> findByLibraryAndStatYearAndStatMonth(Library library, Integer statYear, Integer statMonth);
}

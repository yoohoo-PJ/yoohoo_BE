package com.example.yoohoo_be.dashboard.repository;

import com.example.yoohoo_be.dashboard.domain.BookSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookSnapshotRepository extends JpaRepository<BookSnapshot, Long> {

    // 특정 도서의 월별 스냅샷(누적 대출건수)을 오래된 순으로 조회 — 월별 추이 차트 계산용
    List<BookSnapshot> findAllByBookBookIdOrderBySnapshotYearAscSnapshotMonthAsc(Integer bookId);
}

package com.example.yoohoo_be.dashboard.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DbMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            // 1. 컬럼 존재 여부 확인 후 추가
            String checkColumnsSql = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_NAME = 'library_monthly_stats' AND COLUMN_NAME IN ('idle_books_count', 'damage_pending_count')";
            Integer count = jdbcTemplate.queryForObject(checkColumnsSql, Integer.class);
            
            if (count == null || count < 2) {
                System.out.println("🚀 [DbMigrationRunner] library_monthly_stats 테이블에 통계 컬럼 추가 중...");
                try {
                    jdbcTemplate.execute("ALTER TABLE library_monthly_stats ADD COLUMN idle_books_count INT DEFAULT 0");
                } catch (Exception e) {
                    System.out.println("idle_books_count 컬럼이 이미 존재하거나 에러: " + e.getMessage());
                }
                
                try {
                    jdbcTemplate.execute("ALTER TABLE library_monthly_stats ADD COLUMN damage_pending_count INT DEFAULT 0");
                } catch (Exception e) {
                    System.out.println("damage_pending_count 컬럼이 이미 존재하거나 에러: " + e.getMessage());
                }
            }

            // 2. 지난달 연/월 계산
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.YearMonth lastMonthYM = java.time.YearMonth.from(today.minusMonths(1));
            int lastYear = lastMonthYM.getYear();
            int lastMonth = lastMonthYM.getMonthValue();
            
            // 지난달의 마지막 날짜 구하기 (예: 2026-07-31)
            java.time.LocalDate lastDayOfLastMonth = lastMonthYM.atEndOfMonth();

            // 3. uscore_results 테이블의 실제 데이터를 기반으로 '지난달 말일까지'의 유휴화/파손대기 도서 수 계산
            // (calc_date 기준)
            String idleSql = "SELECT COUNT(*) FROM uscore_results WHERE is_idle = true AND calc_date <= ?";
            Integer realLastMonthIdle = jdbcTemplate.queryForObject(idleSql, Integer.class, lastDayOfLastMonth);
            
            String damageSql = "SELECT COUNT(*) FROM uscore_results WHERE is_idle = true AND inspection_status = 'UNINSPECTED' AND calc_date <= ?";
            Integer realLastMonthDamage = jdbcTemplate.queryForObject(damageSql, Integer.class, lastDayOfLastMonth);

            if (realLastMonthIdle == null) realLastMonthIdle = 0;
            if (realLastMonthDamage == null) realLastMonthDamage = 0;

            // 4. 지난달 통계 로우(Row)에 저장
            System.out.println("🚀 [DbMigrationRunner] 실제 데이터를 기반으로 지난달(" + lastYear + "년 " + lastMonth + "월) 통계 업데이트 중...");
            System.out.println("계산된 유휴화 도서 수: " + realLastMonthIdle + ", 파손 심사 대기 수: " + realLastMonthDamage);

            int updated = jdbcTemplate.update(
                "UPDATE library_monthly_stats SET idle_books_count = ?, damage_pending_count = ? WHERE stat_year = ? AND stat_month = ?",
                realLastMonthIdle, realLastMonthDamage, lastYear, lastMonth
            );
            
            if (updated > 0) {
                System.out.println("✅ [DbMigrationRunner] 지난달 통계 DB 저장 완료!");
            } else {
                System.out.println("⚠️ [DbMigrationRunner] 지난달 통계 row를 찾을 수 없어 업데이트되지 않았습니다.");
            }
            
        } catch (Exception e) {
            System.err.println("❌ [DbMigrationRunner] 작업 실패: " + e.getMessage());
        }
    }
}

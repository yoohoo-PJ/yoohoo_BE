package com.example.yoohoo_be.dashboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "library_monthly_stats")
@Getter
@NoArgsConstructor
public class LibraryMonthlyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @Column(name = "stat_year", nullable = false)
    private Integer statYear;

    @Column(name = "stat_month", nullable = false)
    private Integer statMonth;

    @Column(name = "total_books")
    private Integer totalBooks;

    @Column(name = "total_loans")
    private Integer totalLoans;

    @Column(name = "turnover_rate", precision = 5, scale = 4)
    private BigDecimal turnoverRate;
}

package com.example.yoohoo_be.dashboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "library_kdc_stats")
@Getter
@NoArgsConstructor
public class LibraryKdcStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "kdc_class", nullable = false, length = 1)
    private String kdcClass;

    @Column(name = "holding_count")
    private Integer holdingCount;

    @Column(name = "holding_ratio", precision = 5, scale = 4)
    private BigDecimal holdingRatio;

    @Column(name = "loan_count")
    private Integer loanCount;

    @Column(name = "loan_ratio", precision = 5, scale = 4)
    private BigDecimal loanRatio;

    @Column(name = "demand_supply_gap", precision = 5, scale = 4)
    private BigDecimal demandSupplyGap;
}

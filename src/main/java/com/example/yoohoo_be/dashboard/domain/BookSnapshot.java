package com.example.yoohoo_be.dashboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book_snapshots")
@Getter
@NoArgsConstructor
public class BookSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Long snapshotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "snapshot_year", nullable = false)
    private Short snapshotYear;

    @Column(name = "snapshot_month", nullable = false)
    private Short snapshotMonth;

    @Column(name = "cumulative_loan_count")
    private Integer cumulativeLoanCount;
}

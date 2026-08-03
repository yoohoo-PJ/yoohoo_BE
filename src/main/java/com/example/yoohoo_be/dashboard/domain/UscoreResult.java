package com.example.yoohoo_be.dashboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "uscore_results")
@Getter
@NoArgsConstructor
public class UscoreResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "calc_date", nullable = false)
    private java.time.LocalDate calcDate;

    @Column(name = "age_years")
    private Double ageYears;

    @Column(name = "s_age")
    private Double sAge;

    @Column(name = "v_loan")
    private Double vLoan;

    @Column(name = "s_loan")
    private Double sLoan;

    @Column(name = "recent_1yr_loans")
    private Integer recent1YrLoans;

    @Column(name = "s_decay")
    private Double sDecay;

    @Column(name = "u_score", nullable = false)
    private Double uScore;

    @Column(name = "is_idle")
    private Boolean isIdle;

    @Column(name = "is_dead_book")
    private Boolean isDeadBook;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_status")
    private InspectionStatus inspectionStatus = InspectionStatus.UNINSPECTED;

    @Column(name = "damage_score")
    private Byte damageScore;

    @Column(name = "inspection_notes", columnDefinition = "TEXT")
    private String inspectionNotes;

    @Column(name = "created_at", insertable = false, updatable = false)
    private java.time.LocalDateTime createdAt;
}

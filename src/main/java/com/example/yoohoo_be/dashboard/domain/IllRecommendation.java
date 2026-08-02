package com.example.yoohoo_be.dashboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ill_recommendations")
@Getter
@NoArgsConstructor
public class IllRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long recommendationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uscore_result_id", nullable = false)
    private UscoreResult uscoreResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_library_id", nullable = false)
    private Library originLibrary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dest_library_id", nullable = false)
    private Library destLibrary;

    @Column(name = "`rank`", nullable = false)
    private Byte rank;

    @Column(name = "matching_score", precision = 6, scale = 2, nullable = false)
    private BigDecimal matchingScore;

    @Column(name = "f_dist", precision = 6, scale = 2)
    private BigDecimal fDist;

    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "s_demand", precision = 6, scale = 2)
    private BigDecimal sDemand;

    @Column(name = "s_gap", precision = 6, scale = 2)
    private BigDecimal sGap;

    @Column(name = "s_space", precision = 6, scale = 2)
    private BigDecimal sSpace;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_status")
    private TransferStatus transferStatus = TransferStatus.NONE;

    @Setter
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "transfer_notes", columnDefinition = "TEXT")
    private String transferNotes;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

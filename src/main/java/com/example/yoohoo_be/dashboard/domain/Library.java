package com.example.yoohoo_be.dashboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "libraries")
@Getter
@NoArgsConstructor
public class Library {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "library_id")
    private Integer libraryId;

    @Column(name = "library_code")
    private String libraryCode;

    @Column(name = "library_name")
    private String libraryName;

    @Column(name = "library_type")
    private String libraryType;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "total_books")
    private Integer totalBooks;

    @Column(name = "building_area")
    private BigDecimal buildingArea;

    @Column(name = "data_date")
    private LocalDate dataDate; // 도서관 정보가 마지막으로 집계/갱신된 기준 일자

    // 주소 컬럼 (예: "팔달구", "장안구")
    @Column(name = "address")
    private String address;

    @Column(name = "max_holdings", insertable = false, updatable = false)
    private Integer maxHoldings;
}
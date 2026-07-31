package com.example.yoohoo_be.dashboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "regional_population")
@Getter
@NoArgsConstructor
public class RegionalPopulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Integer regionId;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "region_name")
    private String regionName;

    @Column(name = "total_population")
    private Integer totalPopulation;

    @Column(name = "age_0_9")
    private Integer age0To9;
    
    @Column(name = "age_10_19")
    private Integer age10To19;

    @Column(name = "age_20_29")
    private Integer age20To29;
    
    @Column(name = "age_30_39")
    private Integer age30To39;

    @Column(name = "age_40_49")
    private Integer age40To49;

    @Column(name = "age_50_59")
    private Integer age50To59;

    @Column(name = "age_60_69")
    private Integer age60To69;

    @Column(name = "age_70_79")
    private Integer age70To79;

    @Column(name = "age_80_89")
    private Integer age80To89;

    @Column(name = "age_90_99")
    private Integer age90To99;

    @Column(name = "age_100_plus")
    private Integer age100Plus;
}
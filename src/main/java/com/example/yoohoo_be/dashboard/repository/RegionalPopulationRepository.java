package com.example.yoohoo_be.dashboard.repository;

import com.example.yoohoo_be.dashboard.domain.RegionalPopulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionalPopulationRepository extends JpaRepository<RegionalPopulation, Integer> {
    Optional<RegionalPopulation> findByRegionNameContaining(String regionName);
}
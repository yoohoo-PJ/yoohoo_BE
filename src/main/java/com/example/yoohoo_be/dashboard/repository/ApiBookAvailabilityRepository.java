package com.example.yoohoo_be.dashboard.repository;

import com.example.yoohoo_be.dashboard.domain.ApiBookAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiBookAvailabilityRepository extends JpaRepository<ApiBookAvailability, Long> {
}

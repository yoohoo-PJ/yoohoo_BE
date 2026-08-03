package com.example.yoohoo_be.dashboard.repository;

import com.example.yoohoo_be.dashboard.domain.BookHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookHoldingRepository extends JpaRepository<BookHolding, Integer> {
}

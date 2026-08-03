package com.example.yoohoo_be.dashboard.repository;

import com.example.yoohoo_be.dashboard.domain.BookSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookSnapshotRepository extends JpaRepository<BookSnapshot, Long> {
}

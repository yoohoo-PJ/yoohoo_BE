package com.example.yoohoo_be.dashboard.repository;

import com.example.yoohoo_be.dashboard.domain.LibraryKdcStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryKdcStatRepository extends JpaRepository<LibraryKdcStat, Integer> {

    List<LibraryKdcStat> findByLibrary_LibraryId(Integer libraryId);

    Optional<LibraryKdcStat> findByLibrary_LibraryIdAndKdcClass(Integer libraryId, String kdcClass);
}

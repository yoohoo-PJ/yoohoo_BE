package com.example.yoohoo_be.dashboard.repository;

import com.example.yoohoo_be.dashboard.domain.Library;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibraryRepository extends JpaRepository<Library, Integer> {
    Optional<Library> findByLibraryName(String libraryName);
}
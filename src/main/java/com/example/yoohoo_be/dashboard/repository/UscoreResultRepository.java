package com.example.yoohoo_be.dashboard.repository;

import com.example.yoohoo_be.dashboard.domain.Library;
import com.example.yoohoo_be.dashboard.domain.UscoreResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UscoreResultRepository extends JpaRepository<UscoreResult, Long> {
    
    @Query("SELECT COUNT(u) FROM UscoreResult u WHERE u.library.libraryId = :libraryId AND u.isIdle = true")
    long countByLibraryAndIsIdle(@Param("libraryId") Integer libraryId);

    @Query(value = "SELECT u FROM UscoreResult u " +
           "JOIN FETCH u.book " +
           "WHERE u.isIdle = true " +
           "AND u.inspectionStatus = 'UNINSPECTED'",
           countQuery = "SELECT COUNT(u) FROM UscoreResult u " +
           "WHERE u.isIdle = true " +
           "AND u.inspectionStatus = 'UNINSPECTED'")
    org.springframework.data.domain.Page<UscoreResult> findDamagePendingPage(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(u) FROM UscoreResult u " +
           "WHERE u.isIdle = true " +
           "AND u.inspectionStatus = 'UNINSPECTED' " +
           "AND u.library.libraryId = :libraryId")
    long countDamagePendingByLibrary(@Param("libraryId") Integer libraryId);

    @Query("SELECT COUNT(u) FROM UscoreResult u " +
           "WHERE u.isIdle = true " +
           "AND u.inspectionStatus = :status " +
           "AND u.library.libraryId = :libraryId")
    long countTransferPendingByLibrary(@Param("libraryId") Integer libraryId, @Param("status") com.example.yoohoo_be.dashboard.domain.InspectionStatus status);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE UscoreResult u SET u.isIdle = true, u.calcDate = :today WHERE u.library.libraryId = :libraryId AND u.isIdle = false AND u.uScore >= :threshold")
    int executeIdleClassificationAlgorithm(@Param("libraryId") Integer libraryId, @Param("threshold") Double threshold, @Param("today") java.time.LocalDate today);

    @Query("SELECT COUNT(u) FROM UscoreResult u WHERE u.library.libraryId = :libraryId AND u.isIdle = false")
    long countAnalyzableBooks(@Param("libraryId") Integer libraryId);

    @Query("SELECT u FROM UscoreResult u JOIN FETCH u.book WHERE u.library.libraryId = :libraryId AND u.isIdle = true")
    java.util.List<UscoreResult> findIdleBooksByLibrary(@Param("libraryId") Integer libraryId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE UscoreResult u SET u.calcDate = :today WHERE u.library.libraryId = :libraryId AND u.isIdle = true")
    void updateCalcDateForIdleBooks(@Param("libraryId") Integer libraryId, @Param("today") java.time.LocalDate today);

    @Query("SELECT u FROM UscoreResult u JOIN FETCH u.book WHERE u.library.libraryId = :libraryId AND u.isIdle = true AND u.uScore >= :minScore AND u.uScore < :maxScore")
    java.util.List<UscoreResult> findIdleBooksByScoreRange(@Param("libraryId") Integer libraryId, @Param("minScore") Double minScore, @Param("maxScore") Double maxScore, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT u FROM UscoreResult u JOIN FETCH u.book WHERE u.isIdle = true AND u.inspectionStatus = 'UNINSPECTED' AND u.uScore >= :minScore AND u.uScore < :maxScore")
    java.util.List<UscoreResult> findDamagePendingByScoreRange(@Param("minScore") Double minScore, @Param("maxScore") Double maxScore, org.springframework.data.domain.Pageable pageable);
}

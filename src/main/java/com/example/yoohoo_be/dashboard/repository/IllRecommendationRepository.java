package com.example.yoohoo_be.dashboard.repository;

import com.example.yoohoo_be.dashboard.domain.IllRecommendation;
import com.example.yoohoo_be.dashboard.domain.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IllRecommendationRepository extends JpaRepository<IllRecommendation, Long> {

    @Query(value = "SELECT r FROM IllRecommendation r " +
            "JOIN FETCH r.book " +
            "JOIN FETCH r.originLibrary " +
            "JOIN FETCH r.destLibrary " +
            "WHERE r.transferStatus IN :statuses " +
            "AND r.rank = 1",
            countQuery = "SELECT count(r) FROM IllRecommendation r WHERE r.transferStatus IN :statuses AND r.rank = 1")
    Page<IllRecommendation> findMainRecommendationsByStatuses(@Param("statuses") List<TransferStatus> statuses, Pageable pageable);

    @Query("SELECT r FROM IllRecommendation r " +
            "JOIN FETCH r.book " +
            "JOIN FETCH r.originLibrary " +
            "JOIN FETCH r.destLibrary " +
            "WHERE r.transferStatus IN :statuses " +
            "AND r.uscoreResult.resultId IN :uscoreResultIds " +
            "AND r.rank > 1 " +
            "ORDER BY r.uscoreResult.resultId, r.rank ASC")
    List<IllRecommendation> findAlternativesByUscoreResultIds(@Param("statuses") List<TransferStatus> statuses, @Param("uscoreResultIds") List<Long> uscoreResultIds);

    int countByTransferStatusInAndOriginLibrary_LibraryId(List<TransferStatus> transferStatuses, Integer libraryId);

    int countByTransferStatusInAndDestLibrary_LibraryId(List<TransferStatus> transferStatuses, Integer libraryId);
}

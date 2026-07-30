package com.example.yoohoo_be.checklists.repository;

import com.example.yoohoo_be.checklists.domain.BookCheckResultItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookCheckResultItemRepository extends JpaRepository<BookCheckResultItem, Long> {

    // 특정 점검 그룹(batchId)에 속한 상세 체크 항목 리스트 조회
    List<BookCheckResultItem> findAllByBookCheckBatchId(Long batchId);
}

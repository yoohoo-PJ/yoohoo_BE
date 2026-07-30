package com.example.yoohoo_be.checklists.repository;

import com.example.yoohoo_be.checklists.domain.BookCheckBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookCheckBatchRepository extends JpaRepository<BookCheckBatch, Long> {

    // 특정 도서(bookId)의 역대 점검 기록을 최신순(id 내림차순)으로 모두 조회
    List<BookCheckBatch> findAllByBookIdOrderByIdDesc(Long bookId);

    // 특정 사서(librarianCode)가 수행한 점검 이력 조회 (필요 시 활용)
    List<BookCheckBatch> findAllByLibrarianCode(String librarianCode);
}

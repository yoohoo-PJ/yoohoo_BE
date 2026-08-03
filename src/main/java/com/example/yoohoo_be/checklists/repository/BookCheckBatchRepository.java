package com.example.yoohoo_be.checklists.repository;

import com.example.yoohoo_be.checklists.domain.BookCheckBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookCheckBatchRepository extends JpaRepository<BookCheckBatch, Long> {

    // 특정 도서(bookId)의 역대 점검 기록을 최신순(id 내림차순)으로 모두 조회
    List<BookCheckBatch> findAllByBookBookIdOrderByIdDesc(Integer bookId);

    // 특정 사서(librarianCode)가 수행한 점검 이력 조회 (필요 시 활용)
    List<BookCheckBatch> findAllByLibrarianCode(String librarianCode);

    // 1. [마모 처리 현황 - 첫 번째 화면] 점검 완료 도서 전체 목록 조회용
    @Query("SELECT b FROM BookCheckBatch b JOIN FETCH b.book ORDER BY b.checkedDate DESC, b.id DESC")
    List<BookCheckBatch> findAllCompletedOrderByCheckedDateDesc();

    // 2. [마모 처리 현황 상세 조회용] 특정 도서(bookId)의 가장 최근 점검 내역 1건 조회
    @Query("SELECT b FROM BookCheckBatch b JOIN FETCH b.book WHERE b.book.bookId = :bookId ORDER BY b.checkedDate DESC, b.id DESC LIMIT 1")
    Optional<BookCheckBatch> findLatestByBookId(@Param("bookId") Integer bookId);
}
package com.example.yoohoo_be.dashboard.repository;

import com.example.yoohoo_be.dashboard.domain.Book;
import com.example.yoohoo_be.dashboard.domain.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    // ISBN으로 도서 존재 여부나 도서를 찾을 때 활용
    Optional<Book> findByIsbn(String isbn);

    // 특정 상태(예: IDLE - 마모 점검 대상, IN_PROGRESS - 마모 처리 현황)의 도서 목록만 조회
    List<Book> findAllByStatus(BookStatus status);

    // 특정 상태의 도서 건수 (예: DISCARDED 건수 → 연간 폐기 상한(7%) 검증에 사용)
    long countByStatus(BookStatus status);
}
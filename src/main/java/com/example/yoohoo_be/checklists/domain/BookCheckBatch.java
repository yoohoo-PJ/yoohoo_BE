package com.example.yoohoo_be.checklists.domain;


import com.example.yoohoo_be.dashboard.domain.Book;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "book_check_batches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@EntityListeners(AuditingEntityListener.class)
public class BookCheckBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_batch_id")
    private Long id; // 점검 그룹 고유 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book; // 점검 대상 도서

    @Column(name = "librarian_code", nullable = false, length = 50)
    private String librarianCode; // 점검 수행 사서/담당자 코드

    @Column(name = "checked_date", length = 20)
    private String checkedDate; // 점검 수행 날짜 (YYYY-MM-DD 형식)

    @Column(name = "total_score")
    private Integer totalScore; // 점검 결과 총 점수

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정 일시

    @OneToMany(mappedBy = "bookCheckBatch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookCheckResultItem> items = new ArrayList<>();

    @Builder
    public BookCheckBatch(Book book, String librarianCode, String checkedDate, Integer totalScore) {
        this.book = book;
        this.librarianCode = librarianCode;
        this.checkedDate = checkedDate;
        this.totalScore = totalScore;
    }

    public void addItem(BookCheckResultItem item) {
        this.items.add(item);
        item.setBookCheckBatch(this);
    }

    public void updateBatch(String librarianCode, Integer totalScore) {
        this.librarianCode = librarianCode;
        this.totalScore = totalScore;
    }
}

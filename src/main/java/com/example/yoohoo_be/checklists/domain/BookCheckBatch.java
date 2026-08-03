package com.example.yoohoo_be.checklists.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "librarian_code", nullable = false, length = 50)
    private String librarianCode;

    @Column(name = "checked_date", length = 20)
    private String checkedDate;

    @Column(name = "total_score")
    private Integer totalScore;

    // [제거됨] createdAt -> DBML book_check_batches 테이블에 created_at 컬럼 자체가 없음

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
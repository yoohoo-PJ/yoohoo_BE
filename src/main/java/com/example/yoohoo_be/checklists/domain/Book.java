package com.example.yoohoo_be.checklists.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Book {

    private static final Set<BookStatus> FINAL_DISPOSITION_STATUSES =
            Set.of(BookStatus.DISCARDED, BookStatus.TRANSFERRED, BookStatus.PRESERVED);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;

    // [수정됨] DBML 기준 isbn은 unique + NOT NULL
    @Column(name = "isbn", unique = true, nullable = false, length = 13)
    private String isbn;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    // [수정됨] DBML 기준 author 는 NOT NULL 제약이 없음
    @Column(name = "author")
    private String author;

    // [추가됨] 실제 DB에 있는데 엔티티에 없던 필드
    @Column(name = "kdc_code")
    private String kdcCode;

    // [추가됨]
    @Column(name = "kdc_class")
    private String kdcClass;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "call_number")
    private String callNumber;

    @Column(name = "cover_url")
    private String coverUrl;

    // [제거됨] genre, turnover_rate -> DBML에 존재하지 않는 컬럼이었음
    // (turnover_rate는 book 단위가 아니라 library_monthly_stats 에 도서관 단위로 존재)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookStatus status;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @Column(name = "wear_level")
    private String wearLevel;

    @Column(name = "soil_level")
    private String soilLevel;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Book(String isbn, String title, String author, String kdcCode, String kdcClass,
                String publisher, String callNumber, String coverUrl,
                BookStatus status, Boolean isAvailable) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.kdcCode = kdcCode;
        this.kdcClass = kdcClass;
        this.publisher = publisher;
        this.callNumber = callNumber;
        this.coverUrl = coverUrl;
        this.status = (status != null) ? status : BookStatus.NORMAL;
        this.isAvailable = (isAvailable != null) ? isAvailable : true;
    }

    public void markAsIdle() {
        this.status = BookStatus.IDLE;
        this.isAvailable = false;
    }

    public void completeCheckAndMoveToInProgress(String wearLevel, String soilLevel, Boolean isAvailable) {
        this.status = BookStatus.IN_PROGRESS;
        this.wearLevel = wearLevel;
        this.soilLevel = soilLevel;
        this.isAvailable = isAvailable;
    }

    public void updateCondition(String wearLevel, String soilLevel, Boolean isAvailable) {
        this.wearLevel = wearLevel;
        this.soilLevel = soilLevel;
        this.isAvailable = isAvailable;
    }

    public void decideFinalDisposition(BookStatus decision) {
        if (decision == null || !FINAL_DISPOSITION_STATUSES.contains(decision)) {
            throw new IllegalArgumentException(
                    "최종 처리 결정은 DISCARDED, TRANSFERRED, PRESERVED 중 하나여야 합니다. 입력값: " + decision);
        }
        this.status = decision;
        this.isAvailable = false;
    }
}
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

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id; // 도서 고유 ID (PK)

    @Column(name = "title", nullable = false)
    private String title; // 도서명

    @Column(name = "author", nullable = false)
    private String author; // 저자명

    @Column(name = "publisher")
    private String publisher; // 출판사

    @Column(name = "isbn", unique = true)
    private String isbn; // 도서 ISBN

    @Column(name = "call_number")
    private String callNumber; // 청구기호

    @Column(name = "cover_url")
    private String coverUrl; // 도서 표지 이미지 URL

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookStatus status; // 도서 처리 워크플로우 상태 (NORMAL, IDLE, IN_PROGRESS 등)

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable; // 대출 가능 여부 (true: 대출 가능, false: 보수/폐기 필요)

    @Column(name = "wear_level")
    private String wearLevel; // 책 마모 정도 등급 (GOOD, NORMAL, WORN, SEVERE)

    @Column(name = "soil_level")
    private String soilLevel; // 책 오염 정도 등급 (CLEAN, STAINED, DIRTY)

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 등록 일시

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정 일시

    @Builder
    public Book(String title, String author, String publisher, String isbn,
                String callNumber, String coverUrl, BookStatus status, Boolean isAvailable) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.isbn = isbn;
        this.callNumber = callNumber;
        this.coverUrl = coverUrl;
        this.status = (status != null) ? status : BookStatus.NORMAL;
        this.isAvailable = (isAvailable != null) ? isAvailable : true;
    }

    // ================= 비즈니스 편의 메서드 ================= //

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
}
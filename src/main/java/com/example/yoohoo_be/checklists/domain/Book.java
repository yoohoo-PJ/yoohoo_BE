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

    // 최종 처리 결정으로 허용되는 상태 (폐기/이관/보존 확정 API 에서 검증용)
    private static final Set<BookStatus> FINAL_DISPOSITION_STATUSES =
            Set.of(BookStatus.DISCARDED, BookStatus.TRANSFERRED, BookStatus.PRESERVED);

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

    @Column(name = "genre")
    private String genre; // [추가됨] 도서 장르

    @Column(name = "call_number")
    private String callNumber; // 청구기호

    @Column(name = "cover_url")
    private String coverUrl; // 도서 표지 이미지 URL

    @Column(name = "turnover_rate")
    private Double turnoverRate; // [추가됨] 도서 회전율(대출 빈도 지표) - 산출 로직은 별도 확인 필요

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookStatus status; // 도서 처리 워크플로우 상태

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable; // 대출 가능 여부

    @Column(name = "wear_level")
    private String wearLevel; // 책 마모 정도 등급

    @Column(name = "soil_level")
    private String soilLevel; // 책 오염 정도 등급

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Book(String title, String author, String publisher, String isbn, String genre,
                String callNumber, String coverUrl, Double turnoverRate,
                BookStatus status, Boolean isAvailable) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.isbn = isbn;
        this.genre = genre;
        this.callNumber = callNumber;
        this.coverUrl = coverUrl;
        this.turnoverRate = turnoverRate;
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

    /**
     * [추가됨] 마모 처리 현황에서 사서가 폐기/이관/보존 중 하나로 최종 결정을 확정할 때 호출.
     * React state 로만 관리되던 결정을 DB 에 영속화하기 위한 메서드.
     */
    public void decideFinalDisposition(BookStatus decision) {
        if (decision == null || !FINAL_DISPOSITION_STATUSES.contains(decision)) {
            throw new IllegalArgumentException(
                    "최종 처리 결정은 DISCARDED, TRANSFERRED, PRESERVED 중 하나여야 합니다. 입력값: " + decision);
        }
        this.status = decision;
        this.isAvailable = false; // 세 결정 모두 더 이상 대출 불가 상태로 전환
    }
}
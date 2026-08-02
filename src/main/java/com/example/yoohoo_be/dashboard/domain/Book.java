package com.example.yoohoo_be.dashboard.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Integer bookId;

    @Column(name = "isbn", length = 13, nullable = false, unique = true)
    private String isbn;

    @Column(name = "title", length = 300, nullable = false)
    private String title;

    @Column(name = "author", length = 200)
    private String author;

    @Column(name = "publisher", length = 200)
    private String publisher;

    @Column(name = "kdc_code", length = 10)
    private String kdcCode;

    @Column(name = "kdc_class", length = 1)
    private String kdcClass;

    @Column(name = "call_number")
    private String callNumber;

    @Column(name = "cover_url")
    private String coverUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookStatus status = BookStatus.NORMAL;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "wear_level")
    private String wearLevel;

    @Column(name = "soil_level")
    private String soilLevel;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private java.time.LocalDateTime updatedAt;

    @lombok.Builder
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

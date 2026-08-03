package com.example.yoohoo_be.checklists.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book_check_result_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookCheckResultItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // [수정됨] result_item_id -> id (실제 DB 컬럼명과 일치)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_batch_id", nullable = false)
    private BookCheckBatch bookCheckBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_item_id", nullable = false)
    private CheckItem checkItem;

    @Column(name = "is_passed", nullable = false)
    private Boolean isPassed;

    @Column(name = "item_score")
    private Integer itemScore;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Builder
    public BookCheckResultItem(CheckItem checkItem, Boolean isPassed, Integer itemScore, String note) {
        this.checkItem = checkItem;
        this.isPassed = isPassed;
        this.itemScore = itemScore;
        this.note = note;
    }

    protected void setBookCheckBatch(BookCheckBatch bookCheckBatch) {
        this.bookCheckBatch = bookCheckBatch;
    }

    public void updateItemResult(Boolean isPassed, Integer itemScore, String note) {
        this.isPassed = isPassed;
        this.itemScore = itemScore;
        this.note = note;
    }
}
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
    @Column(name = "result_item_id")
    private Long id; // 항목별 결과 고유 PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_batch_id", nullable = false)
    private BookCheckBatch bookCheckBatch; // 소속된 점검 그룹 (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_item_id", nullable = false)
    private CheckItem checkItem; // 점검 항목 (예: 표지 찢어짐 항목)

    @Column(name = "is_passed", nullable = false)
    private Boolean isPassed; // 양호 여부 (true: 정상, false: 파손/불량)

    @Column(name = "item_score")
    private Integer itemScore; // 해당 항목 판정 점수 (선택적)

    @Column(name = "note", columnDefinition = "TEXT")
    private String note; // 특이사항 메모 (선택적)

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
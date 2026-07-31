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

    @Column(name = "check_item_id", nullable = false)
    private Long checkItemId; // 점검 항목 ID (예: 표지 찢어짐 항목 PK)

    @Column(name = "is_passed", nullable = false)
    private Boolean isPassed; // 양호 여부 (true: 정상, false: 파손/불량)

    @Column(name = "item_score")
    private Integer itemScore; // 해당 항목 판정 점수 (선택적)

    @Column(name = "note", columnDefinition = "TEXT")
    private String note; // 특이사항 메모 (선택적)

    @Builder
    public BookCheckResultItem(Long checkItemId, Boolean isPassed, Integer itemScore, String note) {
        this.checkItemId = checkItemId;
        this.isPassed = isPassed;
        this.itemScore = itemScore;
        this.note = note;
    }

    // BookCheckBatch 연관관계 설정을 위한 setter (패키지-프라이빗 또는 protected 권장)
    protected void setBookCheckBatch(BookCheckBatch bookCheckBatch) {
        this.bookCheckBatch = bookCheckBatch;
    }

    // 항목별 판정 결과 및 메모 수정 메서드 (PUT API 호출 시 사용)
    public void updateItemResult(Boolean isPassed, Integer itemScore, String note) {
        this.isPassed = isPassed;
        this.itemScore = itemScore;
        this.note = note;
    }
}

package com.example.yoohoo_be.checklists.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 점검 항목 마스터 데이터 (예: "표지 찢어짐", "페이지 오염" 등)
 * 사서가 실제로 체크하는 개별 점검 결과(BookCheckResultItem)는
 * 이 CheckItem 을 참조해서 어떤 항목이었는지를 나타낸다.
 */
@Entity
@Table(name = "check_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_item_id")
    private Long id; // 점검 항목 고유 ID (PK)

    @Column(name = "title", nullable = false)
    private String title; // 점검 항목 제목 (예: "표지 찢어짐")

    @Column(name = "category")
    private String category; // 점검 카테고리 (예: BOOK, COVER 등)

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 점검 상세 설명

    @Column(name = "max_score")
    private Integer maxScore; // 해당 항목의 배점(만점) - 참고용 기본값

    @Builder
    public CheckItem(String title, String category, String description, Integer maxScore) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.maxScore = maxScore;
    }
}
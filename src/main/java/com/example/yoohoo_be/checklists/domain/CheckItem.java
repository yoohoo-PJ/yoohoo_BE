package com.example.yoohoo_be.checklists.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "check_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_item_id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "category")
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_score")
    private Integer maxScore;

    @Builder
    public CheckItem(String title, String category, String description, Integer maxScore) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.maxScore = maxScore;
    }
}

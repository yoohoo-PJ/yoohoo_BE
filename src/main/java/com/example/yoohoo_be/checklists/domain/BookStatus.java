package com.example.yoohoo_be.checklists.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookStatus {
    NORMAL("일반 도서"),
    IDLE("유휴화 분류 (마모 점검 대상)"),
    IN_PROGRESS("점검 완료 (마모 처리 현황)"),
    DISCARDED("폐기 도서");

    private final String description;
}

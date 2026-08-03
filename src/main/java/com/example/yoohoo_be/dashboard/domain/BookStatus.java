package com.example.yoohoo_be.dashboard.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookStatus {
    NORMAL("정상 도서"),
    IDLE("유휴 도서"),
    IN_PROGRESS("마모 처리 진행 중"),
    DAMAGE_PENDING("훼손/파손 점검 대기"),
    TRANSFER_PENDING("이관 대기"),
    DISCARDED("폐기"),
    TRANSFERRED("이관"),
    PRESERVED("보존");

    private final String description;
}

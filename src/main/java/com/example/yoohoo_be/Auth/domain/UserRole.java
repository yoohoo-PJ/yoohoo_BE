package com.example.yoohoo_be.Auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Getter
@RequiredArgsConstructor
public enum UserRole implements GrantedAuthority {
    ROLE_LIBRARIAN("ROLE_LIBRARIAN", "도서관 사서"), // 넥서스 프로젝트 성격에 맞춘 명확한 네이밍
    ROLE_ADMIN("ROLE_ADMIN", "도서관 최고 관리자");

    private final String authority;
    private final String description;

    // @Getter가 getAuthority()를 이미 자동 생성해주므로
    // 추가적인 오버라이딩이나 리스트 반환 로직이 전혀 필요 없습니다.
}
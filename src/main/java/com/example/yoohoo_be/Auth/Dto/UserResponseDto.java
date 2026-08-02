package com.example.yoohoo_be.Auth.Dto;

import com.example.yoohoo_be.Auth.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 회원가입 응답용 DTO.
 *
 * User 엔티티를 그대로 반환하면 UserDetails 인터페이스의 isAccountNonExpired(),
 * isAccountNonLocked(), getAuthorities() 같은 불필요한 필드까지 JSON에 같이 노출됩니다.
 * 그래서 응답에 필요한 필드만 별도로 골라 담는 DTO를 만들었습니다.
 */
@Getter
public class UserResponseDto {
    private final Long id;
    private final String email;
    private final String password; // 주의: 아래 클래스 설명 참고
    private final String name;
    private final String nickname;
    private final String librarianCode;
    private final String resetCode;
    private final String refreshToken;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.librarianCode = user.getLibrarianCode();
        this.resetCode = user.getResetCode();
        this.refreshToken = user.getRefreshToken();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}
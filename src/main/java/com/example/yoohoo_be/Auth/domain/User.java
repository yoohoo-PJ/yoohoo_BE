package com.example.yoohoo_be.Auth.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users") // DB 예약어 충돌 방지를 위해 users로 설정된 점 아주 좋습니다.
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    // [추가] 실명 (기존에는 이 필드가 없어서 nickname 값으로 대신 채워 넣던 버그가 있었음)
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    // 사서 식별 코드
    @Column(name = "librarian_code", nullable = false, unique = true)
    private String librarianCode;

    // 권한 필드 (스프링 시큐리티를 위해 필수)
    @Column(name = "user_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    // [추가] 비밀번호 재설정 코드 - 추후 "비밀번호 찾기" 기능 구현 시 사용, 가입 시엔 null
    @Setter
    @Column(name = "reset_code")
    private String resetCode;

    // [추가] 리프레시 토큰 - 추후 Access Token 재발급 기능 구현 시 사용, 가입 시엔 null
    @Setter
    @Column(name = "refresh_token")
    private String refreshToken;

    // [추가] 생성일시 - Hibernate가 INSERT 시 자동으로 채워줌
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // [추가] 수정일시 - Hibernate가 UPDATE 시 자동으로 갱신함
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 생성자 내부 로직 완성 및 권한 고정
    @Builder
    public User(String username, String name, String nickname, String password, String email, String librarianCode) {
        this.username = username;
        this.name = name;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.librarianCode = librarianCode;
        this.userRole = UserRole.ROLE_LIBRARIAN; // 관리자 전용 앱이므로 사서 권한으로 강제 고정
    }

    // --- 아래부터는 UserDetails 인터페이스의 필수 구현 메서드입니다 ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // userRole을 스프링 시큐리티가 이해할 수 있는 형태(GrantedAuthority)로 변환하여 반환
        return List.of(new SimpleGrantedAuthority(this.userRole.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username; // 로그인 시 아이디로 사용할 필드 반환
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정이 만료되지 않았음을 의미
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정이 잠기지 않았음을 의미
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호가 만료되지 않았음을 의미
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정이 활성화된 상태임을 의미
    }
}
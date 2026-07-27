package com.example.yoohoo_be.Auth.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users") // DB 예약어 충돌 방지를 위해 users로 설정된 점 아주 좋습니다.
public class User extends TimeStamped implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    // 1. 방금 논의했던 사서 식별 코드 필수 추가
    @Column(name = "librarian_code", nullable = false, unique = true)
    private String librarianCode;

    // 2. 권한 필드 (스프링 시큐리티를 위해 필수)
    @Column(name = "user_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    // 3. 생성자 내부 로직 완성 및 권한 고정
    @Builder
    public User(String username, String nickname, String password, String email, String librarianCode) {
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.librarianCode = librarianCode;
        this.userRole = UserRole.LIBRARIAN; // 관리자 전용 앱이므로 사서 권한으로 강제 고정
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
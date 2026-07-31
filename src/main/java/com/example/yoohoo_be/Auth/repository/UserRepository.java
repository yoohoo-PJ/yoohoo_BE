package com.example.yoohoo_be.Auth.repository;

import com.example.yoohoo_be.Auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. 로그인 시 스프링 시큐리티가 유저 정보를 찾기 위해 필수적으로 사용하는 메서드
    Optional<User> findByUsername(String username);

    // 2. 회원가입 시 아이디 중복을 검증하기 위한 메서드
    boolean existsByUsername(String username);

    // 3. 회원가입 시 이메일 중복을 검증하기 위한 메서드
    boolean existsByEmail(String email);

    // 4. 회원가입 시 사서 코드(librarianCode) 중복을 검증하기 위한 메서드
    // 한 명의 사서가 여러 계정을 만드는 것을 방지하거나, 이미 등록된 사번인지 확인합니다.
    boolean existsByLibrarianCode(String librarianCode);
}
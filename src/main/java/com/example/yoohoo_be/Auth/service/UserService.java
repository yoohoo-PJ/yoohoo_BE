package com.example.yoohoo_be.Auth.service;

import com.example.yoohoo_be.Auth.Dto.SignUpRequestDto;
import com.example.yoohoo_be.Auth.domain.User;
import com.example.yoohoo_be.Auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(SignUpRequestDto signUpRequestDto) {
        if (userRepository.existsByUsername(signUpRequestDto.getId())) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(signUpRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // [추가] 사서번호 중복 검사 (UserRepository에는 이미 만들어져 있던 메서드인데 기존 코드에서 사용을 안 하고 있었음)
        if (userRepository.existsByLibrarianCode(signUpRequestDto.getLibrarianCode())) {
            throw new IllegalArgumentException("이미 등록된 사서번호입니다.");
        }

        // [수정] name과 nickname을 각각의 값으로 저장 (기존에는 nickname 자리에 name을 넣거나,
        // User 엔티티에 name 컬럼이 없어 name 값 자체가 저장되지 못하던 문제가 있었음)
        User newUser = new User(
                signUpRequestDto.getId(),
                signUpRequestDto.getName(),
                signUpRequestDto.getNickname(),
                passwordEncoder.encode(signUpRequestDto.getPassword()),
                signUpRequestDto.getEmail(),
                signUpRequestDto.getLibrarianCode()
        );

        return userRepository.save(newUser);
    }
}
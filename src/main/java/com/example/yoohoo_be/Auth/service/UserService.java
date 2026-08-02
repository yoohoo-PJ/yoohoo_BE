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

        // [수정] 기존 코드는 nickname 자리에 signUpRequestDto.getName()(실명)을 넣고 있었는데,
        // 회원가입 요청에 별도 nickname 필드가 없어서 실명을 닉네임으로 대신 저장하던 상태였습니다.
        // SignUpRequestDto에 nickname 필드를 추가했으므로 이제 실제 닉네임 값을 저장합니다.
        User newUser = new User(
                signUpRequestDto.getId(),
                signUpRequestDto.getNickname(),
                passwordEncoder.encode(signUpRequestDto.getPassword()),
                signUpRequestDto.getEmail(),
                signUpRequestDto.getLibrarianCode()
        );

        return userRepository.save(newUser);
    }
}
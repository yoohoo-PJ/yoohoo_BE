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

        // [수정] 기존 코드는 5번째 인자로 UserRole.ROLE_LIBRARIAN.name()("ROLE_LIBRARIAN" 문자열)을
        // 넘기고 있었는데, User 생성자의 5번째 인자는 librarianCode(사서번호) 자리입니다.
        // 즉 모든 회원의 사서번호가 "ROLE_LIBRARIAN"이라는 동일한 문자열로 잘못 저장되는 버그였고,
        // unique 제약조건 때문에 두 번째 회원가입부터는 무조건 실패했을 것입니다.
        User newUser = new User(
                signUpRequestDto.getId(),
                signUpRequestDto.getName(),
                passwordEncoder.encode(signUpRequestDto.getPassword()),
                signUpRequestDto.getEmail(),
                signUpRequestDto.getLibrarianCode()
        );

        return userRepository.save(newUser);
    }
}

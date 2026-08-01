package com.example.yoohoo_be.Auth.controller;

import com.example.yoohoo_be.Auth.Dto.LoginRequestDto;
import com.example.yoohoo_be.Auth.Dto.LoginResponseDto;
import com.example.yoohoo_be.Auth.Dto.SignUpRequestDto;
import com.example.yoohoo_be.Auth.Dto.UserResponseDto;
import com.example.yoohoo_be.Auth.domain.User;
import com.example.yoohoo_be.Auth.service.UserService;
import com.example.yoohoo_be.Auth.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("register")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        try {
            User savedUser = userService.registerUser(signUpRequestDto);

            Map<String, Object> body = new HashMap<>();
            body.put("message", "회원가입 성공");
            body.put("user", new UserResponseDto(savedUser));

            return ResponseEntity.status(HttpStatus.CREATED).body(body);

        } catch (IllegalArgumentException e) {
            // 명세서 형태: { "message": "...", "error": "Conflict", "statusCode": 409 }
            Map<String, Object> body = new HashMap<>();
            body.put("message", e.getMessage());
            body.put("error", "Conflict");
            body.put("statusCode", HttpStatus.CONFLICT.value());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);

        } catch (Exception e) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", "서버 오류가 발생했습니다.");
            body.put("error", "Internal Server Error");
            body.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    @PostMapping("login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        try {
            // 인증 객체 생성
            // Authentication Manager를 통해서 username, password 기반으로 인증 수행 지시
            // loadUserByUsername과 passwordEncoder.matches()가 내부적으로 실행
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUsername(),
                            loginRequestDto.getPassword()
                    )
            );
            // 결과물 인증 객체 (UserDetails == Principal)
            // User 엔티티가 UserDetails를 구현하므로, 응답에 필요한 name/email/nickname/librarianCode를
            // 꺼내기 위해 User로 캐스팅
            User user = (User) authentication.getPrincipal();

            // JWT 토큰은 계속 생성해서 헤더로 내려줌 (JwtAuthenticationFilter가 이후 요청 인증에 사용)
            String accessToken = jwtUtil.generateToken(user.getUsername());

            LoginResponseDto responseBody = new LoginResponseDto(
                    user.getNickname(), // name (엔티티에 별도 name 필드가 없어 nickname으로 채움)
                    user.getEmail(),
                    user.getNickname(),
                    user.getLibrarianCode()
            );

            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + accessToken)
                    .body(responseBody);

        } catch (BadCredentialsException e) {
            // 명세서 형태: { "message": "...", "error": "Unauthorized", "statusCode": 401 }
            Map<String, Object> body = new HashMap<>();
            body.put("message", "잘못된 아이디 혹은 비밀번호를 입력했습니다.");
            body.put("error", "Unauthorized");
            body.put("statusCode", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);

        } catch (Exception e) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", "서버 오류가 발생했습니다.");
            body.put("error", "Internal Server Error");
            body.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }
}
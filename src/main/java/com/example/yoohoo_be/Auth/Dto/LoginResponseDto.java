package com.example.yoohoo_be.Auth.Dto;

import lombok.Getter;

/**
 * 로그인 성공 응답용 DTO.
 *
 * 명세서 형태:
 * {
 *   "message": "로그인 성공",
 *   "name": "김사서",
 *   "email": "test3@1example.com",
 *   "nickname": "서수원도서관사서",
 *   "librarianCode": "LIB_12345"
 * }
 */
@Getter
public class LoginResponseDto {
    private final String message;
    private final String name;
    private final String email;
    private final String nickname;
    private final String librarianCode;

    public LoginResponseDto(String name, String email, String nickname, String librarianCode) {
        this.message = "로그인 성공";
        this.name = name;
        this.email = email;
        this.nickname = nickname;
        this.librarianCode = librarianCode;
    }
}
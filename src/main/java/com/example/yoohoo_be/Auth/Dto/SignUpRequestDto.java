package com.example.yoohoo_be.Auth.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * API 명세서 Request Body 형태에 맞춘 회원가입 요청 DTO
 * { "id": "...", "password": "...", "name": "...", "nickname": "...", "email": "...", "librarianCode": "..." }
 */
@Getter
@Setter
public class SignUpRequestDto {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자에서 20자까지 입력해주세요.")
    private String id;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자에서 20자까지 입력해주세요.")
    private String password;

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Size(min = 2, max = 30, message = "이름은 2자에서 30자까지 입력해주세요.")
    private String name;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(min = 2, max = 30, message = "닉네임은 2자에서 30자까지 입력해주세요.")
    private String nickname;

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "사서 번호는 필수 입력 값입니다.")
    private String librarianCode;
}
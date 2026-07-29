package com.example.yoohoo_be.Auth.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponseDto {
    private String username;
    private String accessToken;
}

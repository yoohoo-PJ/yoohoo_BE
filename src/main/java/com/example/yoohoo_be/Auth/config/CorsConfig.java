package com.example.yoohoo_be.Auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // TODO: 실제 프론트엔드 배포 주소
        configuration.setAllowedOrigins(List.of(
                "https://2026-books-data-xi.vercel.app",
                "http://localhost:8080",
                "http://localhost:5173"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        // [핵심] 브라우저 fetch/axios의 res.headers.get("Authorization")이
        // 값을 읽으려면 여기에 명시적으로 노출시켜줘야 함
        configuration.setExposedHeaders(List.of("Authorization"));

        // 쿠키/인증정보 포함 요청 허용 (Authorization 헤더 방식이면 필수는 아니지만
        // 추후 refreshToken을 쿠키로 옮기게 되면 필요해짐)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
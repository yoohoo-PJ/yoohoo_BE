package com.example.yoohoo_be.checklists.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * checklists 도메인 전역 예외 처리기.
 *
 * ⚠️ 프론트(WearManagePage) 명세서 기준으로 응답 포맷이 두 가지로 나뉜다.
 * - 404 / 401 / 500 (프로토콜/인증/시스템 레벨 오류) -> { message, error, statusCode }
 * - 400 (검증 실패, 비즈니스 규칙 위반)             -> { status, message, data: null }
 *
 * 이 두 포맷이 섞여있는 건 프론트 스펙 문서에 있는 그대로를 따른 것이라, 팀 내에서
 * 하나로 통일하고 싶다면 프론트와 다시 협의가 필요하다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ================= 404 Not Found ================= //

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException e) {
        return buildProtocolErrorResponse(HttpStatus.NOT_FOUND, e.getMessage(), "Not Found");
    }

    // ================= 401 / 403 ================= //
    // 실제 로그인/토큰 검증 로직이 이 컨트롤러들에 아직 안 붙어있어서, 지금은
    // Spring Security 인증 예외가 발생할 경로가 없을 수 있음. 인증 붙을 때를 대비해 매핑해둠.

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException e) {
        return buildProtocolErrorResponse(HttpStatus.UNAUTHORIZED,
                "인증이 만료되었습니다. 다시 로그인해 주세요.", "Unauthorized");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException e) {
        return buildProtocolErrorResponse(HttpStatus.FORBIDDEN,
                "해당 작업을 수행할 권한이 없습니다.", "Forbidden");
    }

    // ================= 400 Bad Request ================= //

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequest(InvalidRequestException e) {
        return buildBusinessErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        return buildBusinessErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationFailed(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("message", "요청 값이 올바르지 않습니다.");
        response.put("data", null);
        response.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException e) {
        return buildBusinessErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = String.format("'%s' 파라미터의 값 '%s' 이(가) 올바른 형식이 아닙니다.",
                e.getName(), e.getValue());
        return buildBusinessErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException e) {
        String message = String.format("필수 파라미터 '%s' 가 누락되었습니다.", e.getParameterName());
        return buildBusinessErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return buildBusinessErrorResponse(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.");
    }

    // ================= 500 그 외 모든 예외 ================= //

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception e) {
        return buildProtocolErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 내부 오류가 발생했습니다.", "Internal Server Error");
    }

    // { message, error, statusCode } 포맷 (404/401/403/500)
    private ResponseEntity<Map<String, Object>> buildProtocolErrorResponse(
            HttpStatus status, String message, String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("error", error);
        response.put("statusCode", status.value());
        return ResponseEntity.status(status).body(response);
    }

    // { status, message, data: null } 포맷 (400)
    private ResponseEntity<Map<String, Object>> buildBusinessErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("message", message);
        response.put("data", null);
        return ResponseEntity.status(status).body(response);
    }
}
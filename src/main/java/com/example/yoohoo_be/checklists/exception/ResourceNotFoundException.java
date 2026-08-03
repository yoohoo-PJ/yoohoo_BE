package com.example.yoohoo_be.checklists.exception;

/**
 * 존재하지 않는 리소스(도서, 점검 항목, 점검 이력 등)를 조회/참조하려 할 때 사용.
 * GlobalExceptionHandler 에서 404 Not Found 로 매핑된다.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
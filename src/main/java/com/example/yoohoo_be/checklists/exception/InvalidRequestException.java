package com.example.yoohoo_be.checklists.exception;

/**
 * 요청 자체는 존재하는 리소스를 가리키지만, 값이 비즈니스 규칙에 어긋날 때 사용.
 * 예: decision 값이 DISPOSAL/RELOCATION/KEEP 가 아닌 경우.
 * GlobalExceptionHandler 에서 400 Bad Request 로 매핑된다.
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
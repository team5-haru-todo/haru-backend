package com.haru.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 요청입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_003", "서버 내부 오류가 발생했습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_002", "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),

    // Task
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "TASK_001", "할 일을 찾을 수 없습니다."),
    TASK_ACCESS_DENIED(HttpStatus.FORBIDDEN, "TASK_002", "해당 할 일에 접근할 수 없습니다."),

    // Record
    DAILY_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "RECORD_001", "날짜별 기록을 찾을 수 없습니다."),
    ALREADY_COMPLETED_TODAY(HttpStatus.CONFLICT, "RECORD_002", "오늘의 첫 완료가 이미 처리되었습니다."),
    TODAY_TASK_NOT_SELECTED(HttpStatus.BAD_REQUEST, "RECORD_003", "오늘의 한 개가 설정되어 있지 않습니다."),
    ADDITIONAL_COMPLETION_BEFORE_FIRST(HttpStatus.CONFLICT, "RECORD_004", "첫 완료 전에는 추가 완료를 할 수 없습니다."),
    TASK_ALREADY_COMPLETED_TODAY(HttpStatus.CONFLICT, "RECORD_005", "오늘 이미 완료한 할 일입니다."),

    // Report
    WEEKLY_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_001", "주간 리포트를 찾을 수 없습니다."),

    // Notification
    DEVICE_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_001", "기기 토큰을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
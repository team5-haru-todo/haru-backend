package com.haru.backend.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.haru.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        String code,
        String message,
        List<FieldError> errors,
        LocalDateTime timestamp
) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                false,
                errorCode.getCode(),
                errorCode.getMessage(),
                null,
                LocalDateTime.now()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(
                false,
                errorCode.getCode(),
                message,
                null,
                LocalDateTime.now()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(
                false,
                errorCode.getCode(),
                errorCode.getMessage(),
                errors,
                LocalDateTime.now()
        );
    }

    public record FieldError(
            String field,
            String message
    ) {
    }
}
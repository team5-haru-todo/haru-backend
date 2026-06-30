package com.haru.backend.record.dto;

import jakarta.validation.constraints.NotNull;

public record AdditionalCompleteRequest(

        @NotNull(message = "taskId는 필수입니다.")
        Long taskId
) {
}

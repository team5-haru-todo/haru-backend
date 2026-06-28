package com.haru.backend.task.dto;

import jakarta.validation.constraints.NotNull;

public record TaskRecurringRequest(

        @NotNull(message = "반복 여부는 필수입니다.")
        Boolean recurring
) {
}

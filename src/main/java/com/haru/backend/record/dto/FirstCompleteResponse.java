package com.haru.backend.record.dto;

import java.time.LocalDate;

public record FirstCompleteResponse(
        LocalDate date,
        CompletionResponse completion,
        boolean fireEarned,
        StreakResponse streak
) {
}

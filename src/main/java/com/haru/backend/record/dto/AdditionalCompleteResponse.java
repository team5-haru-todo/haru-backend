package com.haru.backend.record.dto;

import java.time.LocalDate;

public record AdditionalCompleteResponse(
        LocalDate date,
        CompletionResponse completion
) {
}

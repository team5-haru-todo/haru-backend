package com.haru.backend.record.dto;

import java.time.LocalDate;

public record DayCompletionResponse(
        LocalDate date,
        String dayOfWeek,
        boolean completed
) {

    public static DayCompletionResponse of(LocalDate date, boolean completed) {
        return new DayCompletionResponse(date, date.getDayOfWeek().name().substring(0, 3), completed);
    }
}

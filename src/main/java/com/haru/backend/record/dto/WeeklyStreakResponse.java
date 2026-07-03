package com.haru.backend.record.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyStreakResponse(
        LocalDate weekStartDate,
        int todayDayIndex,
        int currentStreak,
        List<DayCompletionResponse> days
) {
}

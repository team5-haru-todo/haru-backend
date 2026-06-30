package com.haru.backend.record.dto;

import com.haru.backend.user.entity.UserStats;

import java.time.LocalDate;

public record StreakResponse(
        int currentStreak,
        int maxStreak,
        int totalSuccessDays,
        LocalDate lastSuccessDate
) {

    public static StreakResponse from(UserStats stats) {
        return new StreakResponse(
                stats.getCurrentStreak(),
                stats.getMaxStreak(),
                stats.getTotalSuccessDays(),
                stats.getLastSuccessDate()
        );
    }

    public static StreakResponse empty() {
        return new StreakResponse(0, 0, 0, null);
    }
}

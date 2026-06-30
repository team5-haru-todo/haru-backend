package com.haru.backend.record.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record TodayResponse(
        LocalDate date,
        CurrentTaskResponse currentTask,
        boolean fireEarned,
        OffsetDateTime firstCompletedAt,
        boolean canFirstComplete,
        boolean canAdditionalComplete,
        List<CompletionResponse> completedTasks
) {
}

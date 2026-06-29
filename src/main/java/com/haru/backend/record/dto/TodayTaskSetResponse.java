package com.haru.backend.record.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TodayTaskSetResponse(
        LocalDate date,
        CurrentTaskResponse currentTask,
        OffsetDateTime currentTaskSelectedAt
) {
}

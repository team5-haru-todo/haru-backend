package com.haru.backend.record.dto;

import com.haru.backend.record.entity.CompletionType;
import com.haru.backend.record.entity.TaskCompletion;
import com.haru.backend.task.entity.TaskType;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public record CompletionResponse(
        Long id,
        Long taskId,
        String content,
        TaskType taskType,
        CompletionType completionType,
        OffsetDateTime completedAt
) {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    public static CompletionResponse from(TaskCompletion completion) {
        return new CompletionResponse(
                completion.getId(),
                completion.getTaskId(),
                completion.getContentSnapshot(),
                TaskType.valueOf(completion.getTaskTypeSnapshot()),
                completion.getCompletionType(),
                toSeoul(completion.getCompletedAt())
        );
    }

    private static OffsetDateTime toSeoul(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, SEOUL);
    }
}

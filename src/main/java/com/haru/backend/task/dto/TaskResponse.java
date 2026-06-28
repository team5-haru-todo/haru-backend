package com.haru.backend.task.dto;

import com.haru.backend.task.entity.Task;
import com.haru.backend.task.entity.TaskType;

import java.time.Instant;

public record TaskResponse(
        Long id,
        String content,
        TaskType taskType,
        int displayOrder,
        Instant createdAt
) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getContent(),
                task.getTaskType(),
                task.getDisplayOrder(),
                task.getCreatedAt()
        );
    }
}

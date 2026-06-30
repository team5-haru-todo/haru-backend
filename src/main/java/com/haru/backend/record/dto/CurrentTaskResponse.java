package com.haru.backend.record.dto;

import com.haru.backend.task.entity.Task;
import com.haru.backend.task.entity.TaskType;

public record CurrentTaskResponse(
        Long id,
        String content,
        TaskType taskType
) {

    public static CurrentTaskResponse from(Task task) {
        return new CurrentTaskResponse(task.getId(), task.getContent(), task.getTaskType());
    }
}

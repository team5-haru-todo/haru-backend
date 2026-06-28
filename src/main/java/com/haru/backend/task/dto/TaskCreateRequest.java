package com.haru.backend.task.dto;

import com.haru.backend.task.entity.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskCreateRequest(

        @NotBlank(message = "할 일 내용은 필수입니다.")
        @Size(max = 255, message = "할 일 내용은 255자를 넘을 수 없습니다.")
        String content,

        TaskType taskType
) {
}

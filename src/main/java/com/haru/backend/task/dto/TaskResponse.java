package com.haru.backend.task.dto;

import com.haru.backend.task.entity.Task;
import com.haru.backend.task.entity.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "할 일 응답")
public record TaskResponse(
        @Schema(description = "할 일 ID", example = "1")
        Long id,

        @Schema(description = "할 일 내용", example = "운동하기")
        String content,

        @Schema(description = "할 일 타입", example = "GENERAL")
        TaskType taskType,

        @Schema(description = "화면 표시 순서", example = "0")
        int displayOrder,

        @Schema(description = "할 일 생성 시각. 프론트에서 '3일 전' 같은 상대 시간 표시에 사용할 수 있다.",
                example = "2026-06-30T00:00:00Z")
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

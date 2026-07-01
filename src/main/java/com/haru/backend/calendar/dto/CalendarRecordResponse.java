package com.haru.backend.calendar.dto;

import com.haru.backend.record.entity.CompletionType;
import com.haru.backend.record.entity.DailyRecord;
import com.haru.backend.record.entity.TaskCompletion;
import com.haru.backend.task.entity.TaskType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

public record CalendarRecordResponse(
        Long recordId,
        LocalDate date,
        boolean fireEarned,
        OffsetDateTime firstCompletedAt,
        List<CompletedTaskResponse> completedTasks
) {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    public static CalendarRecordResponse from(DailyRecord record, List<TaskCompletion> completions) {
        return new CalendarRecordResponse(
                record.getId(),
                record.getRecordDate(),
                record.isFireEarned(),
                toSeoul(record.getFirstCompletedAt()),
                completions.stream()
                        .map(CompletedTaskResponse::from)
                        .toList()
        );
    }

    public record CompletedTaskResponse(
            Long completionId,
            Long taskId,
            String content,
            TaskType taskType,
            CompletionType completionType,
            OffsetDateTime completedAt
    ) {
        public static CompletedTaskResponse from(TaskCompletion completion) {
            // 완료한 뒤 task 내용이 바뀔 수 있어서, 화면에는 완료 당시 저장된 스냅샷을 내려준다.
            return new CompletedTaskResponse(
                    completion.getId(),
                    completion.getTaskId(),
                    completion.getContentSnapshot(),
                    TaskType.valueOf(completion.getTaskTypeSnapshot()),
                    completion.getCompletionType(),
                    toSeoul(completion.getCompletedAt())
            );
        }
    }

    private static OffsetDateTime toSeoul(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, SEOUL);
    }
}

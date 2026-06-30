package com.haru.backend.record.entity;

import com.haru.backend.task.entity.Task;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "task_completions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "daily_record_id", nullable = false, updatable = false)
    private Long dailyRecordId;

    @Column(name = "task_id", nullable = false, updatable = false)
    private Long taskId;

    @Column(name = "content_snapshot", nullable = false, length = 255)
    private String contentSnapshot;

    @Column(name = "task_type_snapshot", nullable = false, length = 20)
    private String taskTypeSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "completion_type", nullable = false, length = 20)
    private CompletionType completionType;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    public static TaskCompletion create(DailyRecord record, Task task, CompletionType type, Instant completedAt) {
        TaskCompletion completion = new TaskCompletion();
        completion.dailyRecordId = record.getId();
        completion.taskId = task.getId();
        completion.contentSnapshot = task.getContent();
        completion.taskTypeSnapshot = task.getTaskType().name();
        completion.completionType = type;
        completion.completedAt = completedAt;
        return completion;
    }
}

package com.haru.backend.record.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DailyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    // raw FK — DB has ON DELETE SET NULL so we manage as plain Long
    @Column(name = "current_task_id")
    private Long currentTaskId;

    @Column(name = "record_date", nullable = false, updatable = false)
    private LocalDate recordDate;

    @Column(name = "fire_earned", nullable = false)
    private boolean fireEarned;

    @Column(name = "current_task_selected_at")
    private Instant currentTaskSelectedAt;

    @Column(name = "first_completed_at")
    private Instant firstCompletedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static DailyRecord create(UUID userId, LocalDate recordDate) {
        DailyRecord record = new DailyRecord();
        record.userId = userId;
        record.recordDate = recordDate;
        record.fireEarned = false;
        return record;
    }

    public void assignTask(Long taskId, Instant selectedAt) {
        this.currentTaskId = taskId;
        this.currentTaskSelectedAt = selectedAt;
    }

    public void clearTask() {
        this.currentTaskId = null;
        this.currentTaskSelectedAt = null;
    }

    public boolean hasFirstCompletion() {
        return this.firstCompletedAt != null;
    }

    public void recordFirstCompletion(Instant completedAt) {
        this.fireEarned = true;
        this.firstCompletedAt = completedAt;
    }
}

package com.haru.backend.record.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserStats {

    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    @Column(name = "max_streak", nullable = false)
    private int maxStreak;

    @Column(name = "total_success_days", nullable = false)
    private int totalSuccessDays;

    @Column(name = "last_success_date")
    private LocalDate lastSuccessDate;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static UserStats create(UUID userId) {
        UserStats stats = new UserStats();
        stats.userId = userId;
        stats.currentStreak = 0;
        stats.maxStreak = 0;
        stats.totalSuccessDays = 0;
        return stats;
    }

    /**
     * 첫 완료 시 호출. today는 Asia/Seoul 기준 오늘 날짜.
     * last_success_date == today이면 중복 호출이므로 아무것도 하지 않는다.
     */
    public void applyFirstCompletion(LocalDate today) {
        if (today.equals(lastSuccessDate)) {
            return;
        }

        if (lastSuccessDate == null || lastSuccessDate.isBefore(today.minusDays(1))) {
            currentStreak = 1;
        } else {
            // lastSuccessDate == yesterday
            currentStreak = currentStreak + 1;
        }

        if (currentStreak > maxStreak) {
            maxStreak = currentStreak;
        }
        totalSuccessDays = totalSuccessDays + 1;
        lastSuccessDate = today;
    }
}

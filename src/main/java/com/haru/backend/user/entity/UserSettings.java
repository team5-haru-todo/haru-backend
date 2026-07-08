package com.haru.backend.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserSettings {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone;

    // 메인 화면 알림 설정 팝업을 이 계정이 이미 봤는지 여부. pushEnabled(실제 수신 설정)와 별개로,
    // 팝업을 다시 띄울지 말지만 결정한다 — 계정 생성 시 기본 false, 팝업/마이페이지에서 알림 설정을
    // 한 번이라도 명시적으로 건드리면 true로 고정된다.
    @Column(name = "notification_prompt_seen", nullable = false)
    private boolean notificationPromptSeen;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static UserSettings createDefault(User user) {
        UserSettings settings = new UserSettings();
        settings.user = user;
        settings.pushEnabled = true;
        settings.timezone = "Asia/Seoul";
        settings.notificationPromptSeen = false;
        return settings;
    }

    public void updatePushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public void updateTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void updateNotificationPromptSeen(boolean notificationPromptSeen) {
        this.notificationPromptSeen = notificationPromptSeen;
    }
}
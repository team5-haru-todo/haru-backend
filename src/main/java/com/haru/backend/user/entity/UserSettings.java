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

    // 메인 empty 화면(3단계) 튜토리얼을 이 계정이 어느 버전까지 봤는지. 계정 생성 시 0이며,
    // 프론트의 현재 튜토리얼 버전보다 낮으면 다시 노출한다. 기기별이 아니라 계정별로 저장해
    // 같은 계정이 다른 기기·재설치 후에도 다시 보지 않게 한다. "생애 최초 완료 여부"와는 무관한
    // 별개 개념이라 hasEverCompleted류 필드와 섞지 않는다.
    @Column(name = "main_tutorial_version", nullable = false)
    private int mainTutorialVersion;

    // 메인 completed 화면(1단계, "한개 더하기" 안내) 튜토리얼 버전. mainTutorialVersion과
    // 서로 독립적으로 시청 여부를 추적한다(한쪽을 이미 봤다고 다른 쪽까지 넘어가지 않음).
    @Column(name = "main_completed_tutorial_version", nullable = false)
    private int mainCompletedTutorialVersion;

    // 메모장 첫 진입 튜토리얼을 이 계정에 더 이상 띄우지 않아도 되는지 여부.
    // 계정 생성 시 기본 false이며, 튜토리얼을 끝까지 보거나 건너뛰면 true로 고정된다.
    @Column(name = "memo_tutorial_seen", nullable = false)
    private boolean memoTutorialSeen;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static UserSettings createDefault(User user) {
        UserSettings settings = new UserSettings();
        settings.user = user;
        settings.pushEnabled = true;
        settings.timezone = "Asia/Seoul";
        settings.notificationPromptSeen = false;
        settings.mainTutorialVersion = 0;
        settings.mainCompletedTutorialVersion = 0;
        settings.memoTutorialSeen = false;
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

    public void updateMainTutorialVersion(int mainTutorialVersion) {
        this.mainTutorialVersion = mainTutorialVersion;
    }

    public void updateMainCompletedTutorialVersion(int mainCompletedTutorialVersion) {
        this.mainCompletedTutorialVersion = mainCompletedTutorialVersion;
    }

    public void updateMemoTutorialSeen(boolean memoTutorialSeen) {
        this.memoTutorialSeen = memoTutorialSeen;
    }
}
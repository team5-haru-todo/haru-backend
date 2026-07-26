package com.haru.backend.user.dto;

import com.haru.backend.user.entity.UserSettings;

public record UserSettingsResponse(
        boolean pushEnabled,
        String timezone,
        boolean notificationPromptSeen,
        boolean memoTutorialSeen
) {
    public static UserSettingsResponse of(UserSettings settings) {
        return new UserSettingsResponse(
                settings.isPushEnabled(),
                settings.getTimezone(),
                settings.isNotificationPromptSeen(),
                settings.isMemoTutorialSeen()
        );
    }
}
package com.haru.backend.user.dto;

import com.haru.backend.user.entity.UserSettings;

public record UserSettingsResponse(
        boolean pushEnabled,
        String timezone,
        boolean notificationPromptSeen,
        int mainTutorialVersion,
        int mainCompletedTutorialVersion,
        boolean memoTutorialSeen,
        boolean memoSlidePreviewSeen
) {
    public static UserSettingsResponse of(UserSettings settings) {
        return new UserSettingsResponse(
                settings.isPushEnabled(),
                settings.getTimezone(),
                settings.isNotificationPromptSeen(),
                settings.getMainTutorialVersion(),
                settings.getMainCompletedTutorialVersion(),
                settings.isMemoTutorialSeen(),
                settings.isMemoSlidePreviewSeen()
        );
    }
}

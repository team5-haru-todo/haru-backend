package com.haru.backend.user.dto;

import com.haru.backend.user.entity.UserSettings;

public record UserSettingsResponse(
        boolean pushEnabled,
        String timezone
) {
    public static UserSettingsResponse of(UserSettings settings) {
        return new UserSettingsResponse(settings.isPushEnabled(), settings.getTimezone());
    }
}
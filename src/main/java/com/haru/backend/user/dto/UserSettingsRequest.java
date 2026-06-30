package com.haru.backend.user.dto;

public record UserSettingsRequest(
        Boolean pushEnabled,
        String timezone
) {}
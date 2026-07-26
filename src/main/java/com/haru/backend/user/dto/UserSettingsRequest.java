package com.haru.backend.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UserSettingsRequest(
        Boolean pushEnabled,
        String timezone,
        Boolean notificationPromptSeen,

        @Min(value = 0, message = "mainTutorialVersion 은 0 이상이어야 합니다.")
        @Max(value = 1000, message = "mainTutorialVersion 이 너무 큽니다.")
        Integer mainTutorialVersion,

        @Min(value = 0, message = "mainCompletedTutorialVersion 은 0 이상이어야 합니다.")
        @Max(value = 1000, message = "mainCompletedTutorialVersion 이 너무 큽니다.")
        Integer mainCompletedTutorialVersion
) {}

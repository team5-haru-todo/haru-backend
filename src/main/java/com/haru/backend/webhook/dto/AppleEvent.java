package com.haru.backend.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleEvent(
        String type,
        String sub,
        @JsonProperty("event_time") Long eventTime
) {
}

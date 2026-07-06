package com.haru.backend.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUnlinkWebhookRequest(
        @JsonProperty("user_id") String userId,
        @JsonProperty("referrer_type") String referrerType
) {
}

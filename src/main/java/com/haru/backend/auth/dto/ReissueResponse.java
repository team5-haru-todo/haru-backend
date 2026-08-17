package com.haru.backend.auth.dto;

public record ReissueResponse(
        String accessToken,
        String refreshToken
) {
}

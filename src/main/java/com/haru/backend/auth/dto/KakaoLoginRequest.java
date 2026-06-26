package com.haru.backend.auth.dto;

import java.time.LocalDateTime;

public record KakaoLoginRequest(
        String accessToken,
        String termsVersion,
        LocalDateTime agreedAt
) {}
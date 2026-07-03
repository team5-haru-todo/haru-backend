package com.haru.backend.auth.dto;

import java.time.LocalDateTime;

public record AppleLoginRequest(
        String identityToken,
        String termsVersion,
        LocalDateTime agreedAt
) {}

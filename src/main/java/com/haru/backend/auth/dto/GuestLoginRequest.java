package com.haru.backend.auth.dto;

import java.time.LocalDateTime;

public record GuestLoginRequest(
        String termsVersion,
        LocalDateTime agreedAt
) {}
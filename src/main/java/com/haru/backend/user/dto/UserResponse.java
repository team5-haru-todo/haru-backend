package com.haru.backend.user.dto;

import com.haru.backend.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String nickname,
        String status,
        List<String> connectedProviders,
        String termsVersion,
        LocalDateTime termsAgreedAt,
        LocalDateTime createdAt
) {
    public static UserResponse of(User user, List<String> connectedProviders) {
        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getStatus(),
                connectedProviders,
                user.getTermsVersion(),
                user.getTermsAgreedAt(),
                user.getCreatedAt()
        );
    }
}
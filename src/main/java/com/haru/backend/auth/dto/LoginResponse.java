package com.haru.backend.auth.dto;

import com.haru.backend.user.entity.User;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserInfo user
) {
    public record UserInfo(
            UUID id,
            String nickname,
            String status,
            List<String> connectedProviders,
            boolean hasSeenOnboarding
    ) {}

    public static LoginResponse of(String accessToken, String refreshToken, User user, List<String> connectedProviders) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                new UserInfo(
                        user.getId(),
                        user.getNickname(),
                        user.getStatus(),
                        connectedProviders,
                        user.isHasSeenOnboarding()
                )
        );
    }
}

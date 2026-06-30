package com.haru.backend.user.service;

import com.haru.backend.global.exception.BusinessException;
import com.haru.backend.global.exception.ErrorCode;
import com.haru.backend.user.dto.UserResponse;
import com.haru.backend.user.dto.UserSettingsRequest;
import com.haru.backend.user.dto.UserSettingsResponse;
import com.haru.backend.user.entity.SocialAccount;
import com.haru.backend.user.entity.User;
import com.haru.backend.user.entity.UserSettings;
import com.haru.backend.user.repository.SocialAccountRepository;
import com.haru.backend.user.repository.UserRepository;
import com.haru.backend.user.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final UserSettingsRepository userSettingsRepository;

    public UserResponse getMe(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<String> connectedProviders = socialAccountRepository.findAllByUser(user).stream()
                .map(SocialAccount::getProvider)
                .toList();

        return UserResponse.of(user, connectedProviders);
    }

    public UserSettingsResponse getMySettings(UUID userId) {
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserSettingsResponse.of(settings);
    }

    @Transactional
    public UserSettingsResponse updateMySettings(UUID userId, UserSettingsRequest request) {
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.pushEnabled() != null) {
            settings.updatePushEnabled(request.pushEnabled());
        }
        if (request.timezone() != null) {
            settings.updateTimezone(request.timezone());
        }

        return UserSettingsResponse.of(settings);
    }
}
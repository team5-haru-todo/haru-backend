package com.haru.backend.user.service;

import com.haru.backend.global.exception.BusinessException;
import com.haru.backend.global.exception.ErrorCode;
import com.haru.backend.user.dto.UserResponse;
import com.haru.backend.user.dto.UserSettingsRequest;
import com.haru.backend.user.dto.UserSettingsResponse;
import com.haru.backend.user.dto.WithdrawRequest;
import com.haru.backend.user.entity.SocialAccount;
import com.haru.backend.user.entity.User;
import com.haru.backend.user.entity.UserSettings;
import com.haru.backend.user.repository.SocialAccountRepository;
import com.haru.backend.user.repository.UserRepository;
import com.haru.backend.user.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
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
        if (request.notificationPromptSeen() != null) {
            settings.updateNotificationPromptSeen(request.notificationPromptSeen());
        }

        return UserSettingsResponse.of(settings);
    }

    @Transactional
    public void withdraw(UUID userId, WithdrawRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        log.info("회원 탈퇴 - userId: {}, reasons: {}, etcReason: {}",
                userId, request.reasons(), request.etcReason());

        // 탈퇴 시 소셜 계정 연동 정보도 함께 정리한다.
        // (재가입 시 같은 소셜 계정으로 다시 연동할 수 있도록 하기 위함 —
        //  탈퇴한 계정이 소셜 고유값을 계속 붙잡고 있으면 새 계정에서 재연동이 막히는 문제가 있었음)
        List<SocialAccount> socialAccounts = socialAccountRepository.findAllByUser(user);
        socialAccountRepository.deleteAll(socialAccounts);

        user.withdraw();
    }

    @Transactional
    public void completeOnboarding(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.completeOnboarding();
    }
}
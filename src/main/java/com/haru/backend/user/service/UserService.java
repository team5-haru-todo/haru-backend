package com.haru.backend.user.service;

import com.haru.backend.devicetoken.repository.DeviceTokenRepository;
import com.haru.backend.global.exception.BusinessException;
import com.haru.backend.global.exception.ErrorCode;
import com.haru.backend.record.repository.DailyRecordRepository;
import com.haru.backend.task.repository.TaskRepository;
import com.haru.backend.user.dto.UserResponse;
import com.haru.backend.user.dto.UserSettingsRequest;
import com.haru.backend.user.dto.UserSettingsResponse;
import com.haru.backend.user.dto.WithdrawRequest;
import com.haru.backend.user.entity.SocialAccount;
import com.haru.backend.user.entity.User;
import com.haru.backend.user.entity.UserSettings;
import com.haru.backend.user.entity.WithdrawalLog;
import com.haru.backend.user.repository.SocialAccountRepository;
import com.haru.backend.user.repository.UserRepository;
import com.haru.backend.user.repository.UserSettingsRepository;
import com.haru.backend.user.repository.WithdrawalLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final WithdrawalLogRepository withdrawalLogRepository;
    private final TaskRepository taskRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final DeviceTokenRepository deviceTokenRepository;

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
        if (request.mainTutorialVersion() != null) {
            // 계정별 "이미 봄" 상태가 실수로(오래된 클라이언트, 요청 순서 뒤바뀜 등) 되돌아가지
            // 않도록, 저장된 값보다 낮은 버전 요청은 거부한다. 동일 값 재요청(멱등)은 허용한다.
            if (request.mainTutorialVersion() < settings.getMainTutorialVersion()) {
                throw new BusinessException(ErrorCode.TUTORIAL_VERSION_DOWNGRADE_NOT_ALLOWED);
            }
            settings.updateMainTutorialVersion(request.mainTutorialVersion());
        }
        if (request.mainCompletedTutorialVersion() != null) {
            if (request.mainCompletedTutorialVersion() < settings.getMainCompletedTutorialVersion()) {
                throw new BusinessException(ErrorCode.TUTORIAL_VERSION_DOWNGRADE_NOT_ALLOWED);
            }
            settings.updateMainCompletedTutorialVersion(request.mainCompletedTutorialVersion());
        }
        if (request.memoTutorialSeen() != null) {
            settings.updateMemoTutorialSeen(request.memoTutorialSeen());
        }
        if (request.memoSlidePreviewSeen() != null) {
            settings.updateMemoSlidePreviewSeen(request.memoSlidePreviewSeen());
        }

        return UserSettingsResponse.of(settings);
    }

    @Transactional
    public void withdraw(UUID userId, WithdrawRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        log.info("회원 탈퇴 - userId: {}, reasons: {}, etcReason: {}",
                userId, request.reasons(), request.etcReason());

        // 탈퇴 사유를 기록한다. (탈퇴 원인 분석용)
        WithdrawalLog withdrawalLog = WithdrawalLog.builder()
                .userId(userId)
                .etcReason(request.etcReason())
                .withdrawnAt(LocalDateTime.now())
                .build();

        List<String> reasons = request.reasons();
        if (reasons != null) {
            reasons.forEach(withdrawalLog::addReason);
        }
        withdrawalLogRepository.save(withdrawalLog);

        // 탈퇴 시 소셜 계정 연동 정보도 함께 정리한다.
        // (재가입 시 같은 소셜 계정으로 다시 연동할 수 있도록 하기 위함 —
        //  탈퇴한 계정이 소셜 고유값을 계속 붙잡고 있으면 새 계정에서 재연동이 막히는 문제가 있었음)
        List<SocialAccount> socialAccounts = socialAccountRepository.findAllByUser(user);
        socialAccountRepository.deleteAll(socialAccounts);

        // 탈퇴 시 유저 데이터를 완전삭제한다. (법적 보관 대상인 withdrawal_log는 별도 보관)
        // 순서 중요: task_completions.task_id는 ON DELETE RESTRICT라서,
        // tasks보다 daily_records를 먼저 지워야 한다.
        // (daily_records를 지우면 task_completions는 ON DELETE CASCADE로 함께 삭제됨)
        dailyRecordRepository.deleteAllByUserId(userId);   // 1. 완료기록 (task_completions는 CASCADE로 자동 삭제)
        taskRepository.deleteAllByUserId(userId);          // 2. 할일·메모
        deviceTokenRepository.deleteAllByUserId(userId);   // 3. 디바이스 토큰

        user.withdraw();
    }

    @Transactional
    public void completeOnboarding(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.completeOnboarding();
    }
}

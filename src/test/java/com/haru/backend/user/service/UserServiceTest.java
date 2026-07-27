package com.haru.backend.user.service;

import com.haru.backend.global.exception.BusinessException;
import com.haru.backend.global.exception.ErrorCode;
import com.haru.backend.user.dto.UserSettingsRequest;
import com.haru.backend.user.dto.UserSettingsResponse;
import com.haru.backend.user.entity.User;
import com.haru.backend.user.entity.UserSettings;
import com.haru.backend.user.repository.SocialAccountRepository;
import com.haru.backend.user.repository.UserRepository;
import com.haru.backend.user.repository.UserSettingsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @InjectMocks
    private UserService userService;

    private final UUID userId = UUID.randomUUID();

    private UserSettings defaultSettings() {
        return UserSettings.createDefault(User.createGuest());
    }

    @Test
    @DisplayName("새로 생성된 설정은 두 튜토리얼 버전이 모두 0이다")
    void getMySettingsDefaultsToZero() {
        given(userSettingsRepository.findById(userId)).willReturn(Optional.of(defaultSettings()));

        UserSettingsResponse response = userService.getMySettings(userId);

        assertThat(response.mainTutorialVersion()).isZero();
        assertThat(response.mainCompletedTutorialVersion()).isZero();
    }

    @Test
    @DisplayName("mainTutorialVersion 만 PATCH 하면 그 필드만 바뀐다")
    void updateMainTutorialVersionOnly() {
        UserSettings settings = defaultSettings();
        given(userSettingsRepository.findById(userId)).willReturn(Optional.of(settings));

        UserSettingsResponse response = userService.updateMySettings(
                userId, new UserSettingsRequest(null, null, null, 1, null, null, null));

        assertThat(response.mainTutorialVersion()).isEqualTo(1);
        assertThat(response.mainCompletedTutorialVersion()).isZero();
    }

    @Test
    @DisplayName("mainCompletedTutorialVersion 만 PATCH 하면 그 필드만 바뀐다")
    void updateMainCompletedTutorialVersionOnly() {
        UserSettings settings = defaultSettings();
        given(userSettingsRepository.findById(userId)).willReturn(Optional.of(settings));

        UserSettingsResponse response = userService.updateMySettings(
                userId, new UserSettingsRequest(null, null, null, null, 1, null, null));

        assertThat(response.mainTutorialVersion()).isZero();
        assertThat(response.mainCompletedTutorialVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("두 튜토리얼 버전을 한 요청에서 동시에 PATCH 할 수 있다")
    void updateBothTutorialVersions() {
        UserSettings settings = defaultSettings();
        given(userSettingsRepository.findById(userId)).willReturn(Optional.of(settings));

        UserSettingsResponse response = userService.updateMySettings(
                userId, new UserSettingsRequest(null, null, null, 1, 1, null, null));

        assertThat(response.mainTutorialVersion()).isEqualTo(1);
        assertThat(response.mainCompletedTutorialVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("튜토리얼 버전만 PATCH 해도 다른 설정 필드는 보존된다")
    void updatingTutorialVersionPreservesOtherFields() {
        UserSettings settings = defaultSettings();
        settings.updatePushEnabled(false);
        settings.updateTimezone("Asia/Tokyo");
        settings.updateNotificationPromptSeen(true);
        given(userSettingsRepository.findById(userId)).willReturn(Optional.of(settings));

        UserSettingsResponse response = userService.updateMySettings(
                userId, new UserSettingsRequest(null, null, null, 1, null, null, null));

        assertThat(response.pushEnabled()).isFalse();
        assertThat(response.timezone()).isEqualTo("Asia/Tokyo");
        assertThat(response.notificationPromptSeen()).isTrue();
    }

    @Test
    @DisplayName("동일 버전으로 재요청해도 멱등하게 같은 값을 유지한다")
    void updateSameVersionIsIdempotent() {
        UserSettings settings = defaultSettings();
        given(userSettingsRepository.findById(userId)).willReturn(Optional.of(settings));

        userService.updateMySettings(userId, new UserSettingsRequest(null, null, null, 1, null, null, null));
        UserSettingsResponse second = userService.updateMySettings(
                userId, new UserSettingsRequest(null, null, null, 1, null, null, null));

        assertThat(second.mainTutorialVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("저장된 값보다 낮은 mainTutorialVersion 요청은 거부한다")
    void rejectsDowngradeOfMainTutorialVersion() {
        UserSettings settings = defaultSettings();
        settings.updateMainTutorialVersion(2);
        given(userSettingsRepository.findById(userId)).willReturn(Optional.of(settings));

        assertThatThrownBy(() -> userService.updateMySettings(
                userId, new UserSettingsRequest(null, null, null, 1, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.TUTORIAL_VERSION_DOWNGRADE_NOT_ALLOWED);
        // 거부된 요청은 값을 바꾸지 않는다
        assertThat(settings.getMainTutorialVersion()).isEqualTo(2);
    }

    @Test
    @DisplayName("저장된 값보다 낮은 mainCompletedTutorialVersion 요청은 거부한다")
    void rejectsDowngradeOfMainCompletedTutorialVersion() {
        UserSettings settings = defaultSettings();
        settings.updateMainCompletedTutorialVersion(2);
        given(userSettingsRepository.findById(userId)).willReturn(Optional.of(settings));

        assertThatThrownBy(() -> userService.updateMySettings(
                userId, new UserSettingsRequest(null, null, null, null, 1, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.TUTORIAL_VERSION_DOWNGRADE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("사용자 A의 튜토리얼 버전 변경이 사용자 B의 설정에 영향을 주지 않는다")
    void tutorialVersionIsIsolatedPerUser() {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();
        UserSettings settingsA = defaultSettings();
        UserSettings settingsB = defaultSettings();
        given(userSettingsRepository.findById(userA)).willReturn(Optional.of(settingsA));
        given(userSettingsRepository.findById(userB)).willReturn(Optional.of(settingsB));

        userService.updateMySettings(userA, new UserSettingsRequest(null, null, null, 1, null, null, null));
        UserSettingsResponse responseB = userService.getMySettings(userB);

        assertThat(settingsA.getMainTutorialVersion()).isEqualTo(1);
        assertThat(responseB.mainTutorialVersion()).isZero();
    }

    @Test
    @DisplayName("새로 생성된 설정은 memoSlidePreviewSeen 이 false 다")
    void getMySettingsDefaultsMemoSlidePreviewSeenToFalse() {
        given(userSettingsRepository.findById(userId)).willReturn(Optional.of(defaultSettings()));

        UserSettingsResponse response = userService.getMySettings(userId);

        assertThat(response.memoSlidePreviewSeen()).isFalse();
    }

    @Test
    @DisplayName("memoSlidePreviewSeen 만 PATCH 하면 그 필드만 바뀐다")
    void updateMemoSlidePreviewSeenOnly() {
        UserSettings settings = defaultSettings();
        given(userSettingsRepository.findById(userId)).willReturn(Optional.of(settings));

        UserSettingsResponse response = userService.updateMySettings(
                userId, new UserSettingsRequest(null, null, null, null, null, null, true));

        assertThat(response.memoSlidePreviewSeen()).isTrue();
        assertThat(response.memoTutorialSeen()).isFalse();
    }
}

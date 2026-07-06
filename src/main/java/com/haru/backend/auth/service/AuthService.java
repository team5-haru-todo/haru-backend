package com.haru.backend.auth.service;

import com.haru.backend.auth.client.AppleTokenVerifier;
import com.haru.backend.auth.client.KakaoAuthClient;
import com.haru.backend.auth.client.KakaoUserInfoResponse;
import com.haru.backend.auth.dto.AppleLoginRequest;
import com.haru.backend.auth.dto.KakaoLoginRequest;
import com.haru.backend.auth.dto.LoginResponse;
import com.haru.backend.global.exception.BusinessException;
import com.haru.backend.global.exception.ErrorCode;
import com.haru.backend.global.security.JwtProvider;
import com.haru.backend.user.entity.SocialAccount;
import com.haru.backend.user.entity.User;
import com.haru.backend.user.entity.UserSettings;
import com.haru.backend.user.entity.UserStats;
import com.haru.backend.user.repository.SocialAccountRepository;
import com.haru.backend.user.repository.UserRepository;
import com.haru.backend.user.repository.UserSettingsRepository;
import com.haru.backend.user.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final UserStatsRepository userStatsRepository;
    private final KakaoAuthClient kakaoAuthClient;
    private final AppleTokenVerifier appleTokenVerifier;
    private final JwtProvider jwtProvider;

    public LoginResponse loginAsGuest() {
        User guest = userRepository.save(User.createGuest());
        createDefaultSettingsAndStats(guest);

        String accessToken = jwtProvider.createAccessToken(guest.getId());
        return LoginResponse.of(accessToken, guest, Collections.emptyList());
    }

    public LoginResponse loginWithKakao(KakaoLoginRequest request) {
        KakaoUserInfoResponse kakaoUser = kakaoAuthClient.getUserInfo(request.accessToken());
        String providerUserId = String.valueOf(kakaoUser.id());

        User user = socialAccountRepository.findByProviderAndProviderUserId("KAKAO", providerUserId)
                .map(SocialAccount::getUser)
                .orElseGet(() -> {
                    User newUser = userRepository.save(
                            User.createFromSocial(kakaoUser.nickname(), request.termsVersion(), request.agreedAt())
                    );
                    socialAccountRepository.save(new SocialAccount(newUser, "KAKAO", providerUserId));
                    createDefaultSettingsAndStats(newUser);
                    return newUser;
                });

        List<String> connectedProviders = socialAccountRepository.findAllByUser(user).stream()
                .map(SocialAccount::getProvider)
                .toList();

        String accessToken = jwtProvider.createAccessToken(user.getId());
        return LoginResponse.of(accessToken, user, connectedProviders);
    }

    public LoginResponse loginWithApple(AppleLoginRequest request) {
        String providerUserId = appleTokenVerifier.verifyAndGetSubject(request.identityToken());

        // Apple은 첫 로그인 이후 닉네임/이메일을 다시 안 내려주는 경우가 많아,
        // 카카오와 달리 신규 유저 닉네임은 기본값으로 채워둔다.
        User user = socialAccountRepository.findByProviderAndProviderUserId("APPLE", providerUserId)
                .map(SocialAccount::getUser)
                .orElseGet(() -> {
                    User newUser = userRepository.save(
                            User.createFromSocial("Apple 사용자", request.termsVersion(), request.agreedAt())
                    );
                    socialAccountRepository.save(new SocialAccount(newUser, "APPLE", providerUserId));
                    createDefaultSettingsAndStats(newUser);
                    return newUser;
                });

        List<String> connectedProviders = socialAccountRepository.findAllByUser(user).stream()
                .map(SocialAccount::getProvider)
                .toList();

        String accessToken = jwtProvider.createAccessToken(user.getId());
        return LoginResponse.of(accessToken, user, connectedProviders);
    }

    /**
     * 게스트 계정에 카카오 계정을 연동한다.
     * 새 유저를 만들지 않고, 현재 로그인된(게스트) 유저에 SocialAccount를 붙이고 ACTIVE로 승격한다.
     */
    @Transactional
    public LoginResponse linkKakao(UUID userId, KakaoLoginRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        KakaoUserInfoResponse kakaoUser = kakaoAuthClient.getUserInfo(request.accessToken());
        String providerUserId = String.valueOf(kakaoUser.id());

        boolean alreadyLinked = socialAccountRepository
                .findByProviderAndProviderUserId("KAKAO", providerUserId)
                .isPresent();
        if (alreadyLinked) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_LINKED);
        }

        socialAccountRepository.save(new SocialAccount(user, "KAKAO", providerUserId));
        user.activate(request.termsVersion(), request.agreedAt());

        List<String> connectedProviders = socialAccountRepository.findAllByUser(user).stream()
                .map(SocialAccount::getProvider)
                .toList();

        String accessToken = jwtProvider.createAccessToken(user.getId());
        return LoginResponse.of(accessToken, user, connectedProviders);
    }

    /**
     * 게스트 계정에 Apple 계정을 연동한다.
     * 새 유저를 만들지 않고, 현재 로그인된(게스트) 유저에 SocialAccount를 붙이고 ACTIVE로 승격한다.
     */
    @Transactional
    public LoginResponse linkApple(UUID userId, AppleLoginRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String providerUserId = appleTokenVerifier.verifyAndGetSubject(request.identityToken());

        boolean alreadyLinked = socialAccountRepository
                .findByProviderAndProviderUserId("APPLE", providerUserId)
                .isPresent();
        if (alreadyLinked) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_LINKED);
        }

        socialAccountRepository.save(new SocialAccount(user, "APPLE", providerUserId));
        user.activate(request.termsVersion(), request.agreedAt());

        List<String> connectedProviders = socialAccountRepository.findAllByUser(user).stream()
                .map(SocialAccount::getProvider)
                .toList();

        String accessToken = jwtProvider.createAccessToken(user.getId());
        return LoginResponse.of(accessToken, user, connectedProviders);
    }

    private void createDefaultSettingsAndStats(User user) {
        userSettingsRepository.save(UserSettings.createDefault(user));
        userStatsRepository.save(UserStats.createDefault(user));
    }
}
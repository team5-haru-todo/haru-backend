package com.haru.backend.auth.service;

import com.haru.backend.auth.client.AppleTokenVerifier;
import com.haru.backend.auth.client.KakaoAuthClient;
import com.haru.backend.auth.client.KakaoUserInfoResponse;
import com.haru.backend.auth.dto.AppleLoginRequest;
import com.haru.backend.auth.dto.KakaoLoginRequest;
import com.haru.backend.auth.dto.LoginResponse;
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

    private void createDefaultSettingsAndStats(User user) {
        userSettingsRepository.save(UserSettings.createDefault(user));
        userStatsRepository.save(UserStats.createDefault(user));
    }
}

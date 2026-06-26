package com.haru.backend.auth.service;

import com.haru.backend.auth.client.KakaoAuthClient;
import com.haru.backend.auth.client.KakaoUserInfoResponse;
import com.haru.backend.auth.dto.KakaoLoginRequest;
import com.haru.backend.auth.dto.LoginResponse;
import com.haru.backend.global.security.JwtProvider;
import com.haru.backend.user.entity.SocialAccount;
import com.haru.backend.user.entity.User;
import com.haru.backend.user.repository.SocialAccountRepository;
import com.haru.backend.user.repository.UserRepository;
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
    private final KakaoAuthClient kakaoAuthClient;
    private final JwtProvider jwtProvider;

    public LoginResponse loginAsGuest() {
        User guest = userRepository.save(User.createGuest());
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
                    return newUser;
                });

        List<String> connectedProviders = socialAccountRepository.findAllByUser(user).stream()
                .map(SocialAccount::getProvider)
                .toList();

        String accessToken = jwtProvider.createAccessToken(user.getId());
        return LoginResponse.of(accessToken, user, connectedProviders);
    }
}
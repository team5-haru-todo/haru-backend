package com.haru.backend.auth.controller;

import com.haru.backend.auth.dto.AppleLoginRequest;
import com.haru.backend.auth.dto.KakaoLoginRequest;
import com.haru.backend.auth.dto.LoginResponse;
import com.haru.backend.auth.dto.SocialUserCheckResponse;
import com.haru.backend.auth.service.AuthService;
import com.haru.backend.global.response.ApiResponse;
import com.haru.backend.global.security.LoginUser;
import com.haru.backend.global.security.TokenInvalidationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;
    private final TokenInvalidationService tokenInvalidationService;

    @PostMapping("/guest")
    public ApiResponse<LoginResponse> loginAsGuest() {
        LoginResponse response = authService.loginAsGuest();
        return ApiResponse.ok("게스트로 시작합니다.", response);
    }

    /**
     * 카카오 SDK 로그인 직후, 실제 가입 처리를 하기 전에 신규 유저인지 확인한다.
     * 신규면 프론트에서 약관 동의 화면을 보여준 뒤 /kakao로 다시 요청해야 한다.
     */
    @PostMapping("/kakao/check")
    public ApiResponse<SocialUserCheckResponse> checkKakaoUser(@RequestParam("accessToken") String accessToken) {
        SocialUserCheckResponse response = authService.checkKakaoUser(accessToken);
        return ApiResponse.ok(response);
    }

    /**
     * Apple SDK 로그인 직후, 실제 가입 처리를 하기 전에 신규 유저인지 확인한다.
     */
    @PostMapping("/apple/check")
    public ApiResponse<SocialUserCheckResponse> checkAppleUser(@RequestParam("identityToken") String identityToken) {
        SocialUserCheckResponse response = authService.checkAppleUser(identityToken);
        return ApiResponse.ok(response);
    }

    @PostMapping("/kakao")
    public ApiResponse<LoginResponse> loginWithKakao(@RequestBody KakaoLoginRequest request) {
        LoginResponse response = authService.loginWithKakao(request);
        return ApiResponse.ok("로그인에 성공했습니다.", response);
    }

    @PostMapping("/apple")
    public ApiResponse<LoginResponse> loginWithApple(@RequestBody AppleLoginRequest request) {
        LoginResponse response = authService.loginWithApple(request);
        return ApiResponse.ok("로그인에 성공했습니다.", response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@LoginUser UUID userId, HttpServletRequest request) {
        resolveToken(request).ifPresent(tokenInvalidationService::invalidate);
        return ApiResponse.ok("로그아웃 되었습니다.", null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/kakao/link")
    public ApiResponse<LoginResponse> linkKakao(
            @LoginUser UUID userId,
            @RequestBody KakaoLoginRequest request
    ) {
        LoginResponse response = authService.linkKakao(userId, request);
        return ApiResponse.ok("카카오 계정이 연동되었습니다.", response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/apple/link")
    public ApiResponse<LoginResponse> linkApple(
            @LoginUser UUID userId,
            @RequestBody AppleLoginRequest request
    ) {
        LoginResponse response = authService.linkApple(userId, request);
        return ApiResponse.ok("Apple 계정이 연동되었습니다.", response);
    }

    private Optional<String> resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return Optional.of(header.substring(BEARER_PREFIX.length()));
        }
        return Optional.empty();
    }
}
package com.haru.backend.auth.controller;

import com.haru.backend.auth.dto.AppleLoginRequest;
import com.haru.backend.auth.dto.KakaoLoginRequest;
import com.haru.backend.auth.dto.LinkAppleRequest;
import com.haru.backend.auth.dto.LinkKakaoRequest;
import com.haru.backend.auth.dto.LoginResponse;
import com.haru.backend.auth.service.AuthService;
import com.haru.backend.global.response.ApiResponse;
import com.haru.backend.global.security.LoginUser;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/guest")
    public ApiResponse<LoginResponse> loginAsGuest() {
        LoginResponse response = authService.loginAsGuest();
        return ApiResponse.ok("게스트로 시작합니다.", response);
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

    // 현재 로그인된(게스트 포함) 유저 계정에 카카오 계정을 연동한다.
    // 새 계정을 만드는 게 아니라, 기존 계정에 로그인 수단을 추가하는 것이라 기록이 그대로 유지된다.
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/link/kakao")
    public ApiResponse<LoginResponse> linkKakao(@LoginUser UUID userId, @RequestBody LinkKakaoRequest request) {
        LoginResponse response = authService.linkKakao(userId, request);
        return ApiResponse.ok("카카오 계정이 연동되었습니다.", response);
    }

    // 현재 로그인된(게스트 포함) 유저 계정에 Apple 계정을 연동한다.
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/link/apple")
    public ApiResponse<LoginResponse> linkApple(@LoginUser UUID userId, @RequestBody LinkAppleRequest request) {
        LoginResponse response = authService.linkApple(userId, request);
        return ApiResponse.ok("Apple 계정이 연동되었습니다.", response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@LoginUser UUID userId) {
        return ApiResponse.ok("로그아웃 되었습니다.", null);
    }
}

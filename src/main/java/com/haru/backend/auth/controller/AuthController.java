package com.haru.backend.auth.controller;

import com.haru.backend.auth.dto.KakaoLoginRequest;
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

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@LoginUser UUID userId) {
        return ApiResponse.ok("로그아웃 되었습니다.", null);
    }
}

package com.haru.backend.auth.controller;

import com.haru.backend.auth.dto.LoginResponse;
import com.haru.backend.auth.service.AuthService;
import com.haru.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
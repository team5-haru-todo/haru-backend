package com.haru.backend.user.controller;

import com.haru.backend.global.response.ApiResponse;
import com.haru.backend.global.security.LoginUser;
import com.haru.backend.global.security.TokenInvalidationService;
import com.haru.backend.user.dto.UserResponse;
import com.haru.backend.user.dto.UserSettingsRequest;
import com.haru.backend.user.dto.UserSettingsResponse;
import com.haru.backend.user.dto.WithdrawRequest;
import com.haru.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserService userService;
    private final TokenInvalidationService tokenInvalidationService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(@LoginUser UUID userId) {
        UserResponse response = userService.getMe(userId);
        return ApiResponse.ok(response);
    }

    @GetMapping("/me/settings")
    public ApiResponse<UserSettingsResponse> getMySettings(@LoginUser UUID userId) {
        UserSettingsResponse response = userService.getMySettings(userId);
        return ApiResponse.ok(response);
    }

    @PatchMapping("/me/settings")
    public ApiResponse<UserSettingsResponse> updateMySettings(
            @LoginUser UUID userId,
            @RequestBody UserSettingsRequest request
    ) {
        UserSettingsResponse response = userService.updateMySettings(userId, request);
        return ApiResponse.ok("설정이 수정되었습니다.", response);
    }

    @PostMapping("/me/withdraw")
    public ApiResponse<Void> withdraw(
            @LoginUser UUID userId,
            @RequestBody WithdrawRequest request,
            HttpServletRequest httpRequest
    ) {
        userService.withdraw(userId, request);
        resolveToken(httpRequest).ifPresent(tokenInvalidationService::invalidate);
        return ApiResponse.ok("탈퇴가 완료되었습니다.", null);
    }

    @PostMapping("/me/onboarding")
    public ApiResponse<Void> completeOnboarding(@LoginUser UUID userId) {
        userService.completeOnboarding(userId);
        return ApiResponse.ok("온보딩이 완료되었습니다.", null);
    }

    private Optional<String> resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return Optional.of(header.substring(BEARER_PREFIX.length()));
        }
        return Optional.empty();
    }
}
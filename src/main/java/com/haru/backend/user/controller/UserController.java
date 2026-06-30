package com.haru.backend.user.controller;

import com.haru.backend.global.response.ApiResponse;
import com.haru.backend.global.security.LoginUser;
import com.haru.backend.user.dto.UserResponse;
import com.haru.backend.user.dto.UserSettingsRequest;
import com.haru.backend.user.dto.UserSettingsResponse;
import com.haru.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
}
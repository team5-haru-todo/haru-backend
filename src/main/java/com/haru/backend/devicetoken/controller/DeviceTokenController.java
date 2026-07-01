package com.haru.backend.devicetoken.controller;

import com.haru.backend.devicetoken.dto.DeviceTokenRequest;
import com.haru.backend.devicetoken.service.DeviceTokenService;
import com.haru.backend.global.response.ApiResponse;
import com.haru.backend.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceTokenController {
    private final DeviceTokenService deviceTokenService;

    //토큰 저장
    @PostMapping("/token")
    public ApiResponse<Void> saveToken(
            @LoginUser UUID userId,
            @Valid @RequestBody DeviceTokenRequest request
    ) {
        // 로그인한 사용자와 현재 기기의 FCM 토큰을 연결한다.
        deviceTokenService.saveToken(userId, request);
        return ApiResponse.ok();
    }
}

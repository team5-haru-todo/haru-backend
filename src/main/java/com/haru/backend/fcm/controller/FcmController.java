package com.haru.backend.fcm.controller;

import com.haru.backend.fcm.FcmService;
import com.haru.backend.fcm.FcmTestRequest;
import com.haru.backend.global.response.ApiResponse;
import com.haru.backend.global.security.LoginUser;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Profile("!prod")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
public class FcmController {
    private final FcmService fcmService;

    @PostMapping("/test")
    //응답만 줘도 돼서 반환 타입 void 임
    public ApiResponse<Void> test(@LoginUser UUID userId, @RequestBody FcmTestRequest request){
        log.info("FCM 수동 테스트 발송 - 요청자: {}, 대상 토큰: {}", userId, request.token());
        fcmService.sendMessage("manual-test", request.token(), request.title(), request.body());
        return ApiResponse.ok();
    }
}
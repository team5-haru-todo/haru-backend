package com.haru.backend.auth.controller;

import com.haru.backend.auth.dto.WidgetTokenResponse;
import com.haru.backend.auth.dto.WidgetTokenRotateRequest;
import com.haru.backend.auth.token.WidgetTokenService;
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
@RequiredArgsConstructor
@RequestMapping("/api/widget/token")
public class WidgetTokenController {

    private final WidgetTokenService widgetTokenService;

    //발급 - 인증 필요
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ApiResponse<WidgetTokenResponse> issue(@LoginUser UUID userId) {
        String rawToken = widgetTokenService.issue(userId);
        return ApiResponse.ok(new WidgetTokenResponse(rawToken));
    }

    //회전- 위젯 토큰 자체로 인증, bearer 없음
    @PostMapping("/rotate")
    public ApiResponse<WidgetTokenResponse> rotate(@RequestBody WidgetTokenRotateRequest request){
        String newToken = widgetTokenService.rotate(request.widgetToken());
        return ApiResponse.ok(new WidgetTokenResponse(newToken));
    }
}

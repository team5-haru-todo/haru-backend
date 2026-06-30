package com.haru.backend.record.controller;

import com.haru.backend.global.response.ApiResponse;
import com.haru.backend.global.security.LoginUser;
import com.haru.backend.record.dto.StreakResponse;
import com.haru.backend.record.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Streak", description = "스트릭 조회 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StreakController {

    private final RecordService recordService;

    @Operation(summary = "스트릭 조회")
    @GetMapping("/streak")
    public ApiResponse<StreakResponse> getStreak(@LoginUser UUID userId) {
        return ApiResponse.ok(recordService.getStreak(userId));
    }
}

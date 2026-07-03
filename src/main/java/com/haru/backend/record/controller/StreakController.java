package com.haru.backend.record.controller;

import com.haru.backend.global.response.ApiResponse;
import com.haru.backend.global.security.LoginUser;
import com.haru.backend.record.dto.StreakResponse;
import com.haru.backend.record.dto.WeeklyStreakResponse;
import com.haru.backend.record.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Streak", description = "스트릭 조회 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StreakController {

    private final RecordService recordService;

    @Operation(summary = "스트릭 조회", description = "사용자의 현재 스트릭과 누적 성공 일수를 반환한다. 첫 완료 기준으로 갱신된다.")
    @GetMapping("/streak")
    public ApiResponse<StreakResponse> getStreak(@LoginUser UUID userId) {
        return ApiResponse.ok(recordService.getStreak(userId));
    }

    @Operation(summary = "주간 완료 여부 조회", description = "이번 주(월~일, Asia/Seoul) 날짜별 완료 여부와 현재 스트릭을 반환한다.")
    @GetMapping("/streak/week")
    public ApiResponse<WeeklyStreakResponse> getWeeklyStreak(@LoginUser UUID userId) {
        return ApiResponse.ok(recordService.getWeeklyStreak(userId));
    }
}

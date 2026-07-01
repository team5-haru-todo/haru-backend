package com.haru.backend.record.controller;

import com.haru.backend.global.response.ApiResponse;
import com.haru.backend.global.security.LoginUser;
import com.haru.backend.record.dto.*;
import com.haru.backend.record.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Today", description = "오늘의 한 개 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/today")
@RequiredArgsConstructor
public class TodayController {

    private final RecordService recordService;

    @Operation(summary = "오늘 화면 조회", description = "오늘 날짜 기준(Asia/Seoul) 오늘의 한 개 상태와 완료 목록을 반환한다.")
    @GetMapping
    public ApiResponse<TodayResponse> getToday(@LoginUser UUID userId) {
        return ApiResponse.ok(recordService.getToday(userId));
    }

    @Operation(summary = "새 할 일 생성 및 오늘의 한 개 설정", description = "새 할 일을 생성하고 오늘의 한 개로 설정한다. taskType 기본값은 GENERAL.")
    @PostMapping("/task")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TodayTaskSetResponse> createTodayTask(
            @LoginUser UUID userId,
            @Valid @RequestBody CreateTodayTaskRequest request
    ) {
        return ApiResponse.ok("오늘의 한 개가 설정되었습니다.", recordService.createTodayTask(userId, request));
    }

    @Operation(summary = "기존 할 일을 오늘의 한 개로 설정", description = "이미 존재하는 할 일을 오늘의 한 개로 설정하거나 교체한다.")
    @PatchMapping("/task")
    public ApiResponse<TodayTaskSetResponse> setTodayTask(
            @LoginUser UUID userId,
            @Valid @RequestBody SetTodayTaskRequest request
    ) {
        return ApiResponse.ok("오늘의 한 개가 설정되었습니다.", recordService.setTodayTask(userId, request));
    }

    @Operation(summary = "오늘의 한 개 해제", description = "오늘의 한 개를 해제한다. 완료 기록과 불꽃은 유지된다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "오늘의 한 개 해제 완료",
            content = @Content
    )
    @DeleteMapping("/task")
    public ResponseEntity<Void> clearTodayTask(@LoginUser UUID userId) {
        recordService.clearTodayTask(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "첫 완료 처리", description = "오늘의 한 개를 첫 완료 처리한다. 불꽃과 스트릭이 갱신된다.")
    @PostMapping("/complete")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FirstCompleteResponse> completeFirst(@LoginUser UUID userId) {
        return ApiResponse.ok("오늘의 첫 완료가 처리되었습니다.", recordService.completeFirst(userId));
    }

    @Operation(summary = "추가 완료 처리", description = "첫 완료 이후 다른 할 일을 추가로 완료 처리한다. 스트릭은 변경되지 않는다.")
    @PostMapping("/additional-complete")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdditionalCompleteResponse> completeAdditional(
            @LoginUser UUID userId,
            @Valid @RequestBody AdditionalCompleteRequest request
    ) {
        return ApiResponse.ok("추가 완료가 처리되었습니다.", recordService.completeAdditional(userId, request));
    }
}

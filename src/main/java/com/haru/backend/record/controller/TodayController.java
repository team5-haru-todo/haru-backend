package com.haru.backend.record.controller;

import com.haru.backend.global.response.ApiResponse;
import com.haru.backend.global.security.LoginUser;
import com.haru.backend.record.dto.*;
import com.haru.backend.record.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Today", description = "오늘의 한 개 API")
@RestController
@RequestMapping("/api/today")
@RequiredArgsConstructor
public class TodayController {

    private final RecordService recordService;

    @Operation(summary = "오늘 화면 조회")
    @GetMapping
    public ApiResponse<TodayResponse> getToday(@LoginUser UUID userId) {
        return ApiResponse.ok(recordService.getToday(userId));
    }

    @Operation(summary = "새 할 일 생성 및 오늘의 한 개 설정")
    @PostMapping("/task")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TodayTaskSetResponse> createTodayTask(
            @LoginUser UUID userId,
            @Valid @RequestBody CreateTodayTaskRequest request
    ) {
        return ApiResponse.ok("오늘의 한 개가 설정되었습니다.", recordService.createTodayTask(userId, request));
    }

    @Operation(summary = "기존 할 일을 오늘의 한 개로 설정")
    @PatchMapping("/task")
    public ApiResponse<TodayTaskSetResponse> setTodayTask(
            @LoginUser UUID userId,
            @Valid @RequestBody SetTodayTaskRequest request
    ) {
        return ApiResponse.ok("오늘의 한 개가 설정되었습니다.", recordService.setTodayTask(userId, request));
    }

    @Operation(summary = "오늘의 한 개 해제")
    @DeleteMapping("/task")
    public ResponseEntity<Void> clearTodayTask(@LoginUser UUID userId) {
        recordService.clearTodayTask(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "첫 완료 처리")
    @PostMapping("/complete")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FirstCompleteResponse> completeFirst(@LoginUser UUID userId) {
        return ApiResponse.ok("오늘의 첫 완료가 처리되었습니다.", recordService.completeFirst(userId));
    }

    @Operation(summary = "추가 완료 처리")
    @PostMapping("/additional-complete")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdditionalCompleteResponse> completeAdditional(
            @LoginUser UUID userId,
            @Valid @RequestBody AdditionalCompleteRequest request
    ) {
        return ApiResponse.ok("추가 완료가 처리되었습니다.", recordService.completeAdditional(userId, request));
    }
}

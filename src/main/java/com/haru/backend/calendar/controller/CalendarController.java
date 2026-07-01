package com.haru.backend.calendar.controller;

import com.haru.backend.calendar.dto.CalendarRecordResponse;
import com.haru.backend.calendar.service.CalendarService;
import com.haru.backend.global.response.ApiResponse;
import com.haru.backend.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/calendar")
public class CalendarController {
    private final CalendarService calendarService;

    @GetMapping
    public ApiResponse<List<CalendarRecordResponse>> getMonthly(
            @LoginUser UUID userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        // 달력 화면은 로그인한 사용자의 해당 월 기록만 내려준다.
        List<CalendarRecordResponse> records = calendarService.getMonthlyRecords(userId, year, month);
        return ApiResponse.ok(records);
    }
}

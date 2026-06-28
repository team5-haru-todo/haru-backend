package com.haru.backend.task.controller;

import com.haru.backend.global.response.ApiResponse;
import com.haru.backend.global.security.LoginUser;
import com.haru.backend.task.dto.TaskCreateRequest;
import com.haru.backend.task.dto.TaskResponse;
import com.haru.backend.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskResponse> create(
            @LoginUser UUID userId,
            @Valid @RequestBody TaskCreateRequest request
    ) {
        TaskResponse response = taskService.create(userId, request);
        return ApiResponse.ok("할 일이 생성되었습니다.", response);
    }

    @GetMapping
    public ApiResponse<List<TaskResponse>> getTasks(@LoginUser UUID userId) {
        List<TaskResponse> responses = taskService.getTasks(userId);
        return ApiResponse.ok(responses);
    }
}

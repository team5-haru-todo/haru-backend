package com.haru.backend.task.controller;

import com.haru.backend.global.response.ApiResponse;
import com.haru.backend.global.security.LoginUser;
import com.haru.backend.task.dto.TaskCreateRequest;
import com.haru.backend.task.dto.TaskOrderRequest;
import com.haru.backend.task.dto.TaskRecurringRequest;
import com.haru.backend.task.dto.TaskResponse;
import com.haru.backend.task.dto.TaskUpdateRequest;
import com.haru.backend.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PatchMapping("/{taskId}")
    public ApiResponse<TaskResponse> update(
            @LoginUser UUID userId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request
    ) {
        TaskResponse response = taskService.updateContent(userId, taskId, request);
        return ApiResponse.ok("할 일이 수정되었습니다.", response);
    }

    @DeleteMapping("/{taskId}")
    public ApiResponse<Void> delete(
            @LoginUser UUID userId,
            @PathVariable Long taskId
    ) {
        taskService.delete(userId, taskId);
        return ApiResponse.ok("할 일이 삭제되었습니다.", null);
    }

    @PatchMapping("/{taskId}/recurring")
    public ApiResponse<TaskResponse> changeRecurring(
            @LoginUser UUID userId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRecurringRequest request
    ) {
        TaskResponse response = taskService.changeRecurring(userId, taskId, request.recurring());
        return ApiResponse.ok("반복 설정이 변경되었습니다.", response);
    }

    @PatchMapping("/order")
    public ApiResponse<Void> updateOrder(
            @LoginUser UUID userId,
            @Valid @RequestBody TaskOrderRequest request
    ) {
        taskService.updateOrder(userId, request.orders());
        return ApiResponse.ok("할 일 순서가 변경되었습니다.", null);
    }
}

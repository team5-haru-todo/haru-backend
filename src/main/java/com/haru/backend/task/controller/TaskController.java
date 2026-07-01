package com.haru.backend.task.controller;

import com.haru.backend.global.response.ApiResponse;
import com.haru.backend.global.security.LoginUser;
import com.haru.backend.task.dto.TaskCreateRequest;
import com.haru.backend.task.dto.TaskOrderRequest;
import com.haru.backend.task.dto.TaskRecurringRequest;
import com.haru.backend.task.dto.TaskResponse;
import com.haru.backend.task.dto.TaskUpdateRequest;
import com.haru.backend.task.service.TaskService;
import com.haru.backend.global.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Task", description = "할 일 원본 관리 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "할 일 생성", description = "새 할 일을 생성한다. taskType 기본값은 GENERAL.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskResponse> create(
            @Parameter(hidden = true) @LoginUser UUID userId,
            @Valid @RequestBody TaskCreateRequest request
    ) {
        TaskResponse response = taskService.create(userId, request);
        return ApiResponse.ok("할 일이 생성되었습니다.", response);
    }

    @Operation(summary = "할 일 목록 조회", description = "삭제되지 않은 할 일을 표시 순서대로 조회한다. 응답에는 상대 시간 표시용 createdAt 이 포함된다.")
    @GetMapping
    public ApiResponse<List<TaskResponse>> getTasks(@Parameter(hidden = true) @LoginUser UUID userId) {
        List<TaskResponse> responses = taskService.getTasks(userId);
        return ApiResponse.ok(responses);
    }

    @Operation(summary = "할 일 수정", description = "할 일 내용을 수정한다. 본인 소유만 가능.")
    @PatchMapping("/{taskId}")
    public ApiResponse<TaskResponse> update(
            @Parameter(hidden = true) @LoginUser UUID userId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request
    ) {
        TaskResponse response = taskService.updateContent(userId, taskId, request);
        return ApiResponse.ok("할 일이 수정되었습니다.", response);
    }

    @Operation(summary = "할 일 삭제", description = "할 일을 soft delete 한다. 본인 소유만 가능.")
    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(hidden = true) @LoginUser UUID userId,
            @PathVariable Long taskId
    ) {
        taskService.delete(userId, taskId);
    }

    @Operation(summary = "반복 설정 변경", description = "recurring 값에 따라 taskType 을 RECURRING/GENERAL 로 변경한다.")
    @PatchMapping("/{taskId}/recurring")
    public ApiResponse<TaskResponse> changeRecurring(
            @Parameter(hidden = true) @LoginUser UUID userId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRecurringRequest request
    ) {
        TaskResponse response = taskService.changeRecurring(userId, taskId, request.recurring());
        return ApiResponse.ok("반복 설정이 변경되었습니다.", response);
    }

    @Operation(summary = "할 일 순서 변경", description = "여러 할 일의 표시 순서(displayOrder)를 일괄 변경한다.")
    @PatchMapping("/order")
    public ApiResponse<Void> updateOrder(
            @Parameter(hidden = true) @LoginUser UUID userId,
            @Valid @RequestBody TaskOrderRequest request
    ) {
        taskService.updateOrder(userId, request.orders());
        return ApiResponse.ok("할 일 순서가 변경되었습니다.", null);
    }
}

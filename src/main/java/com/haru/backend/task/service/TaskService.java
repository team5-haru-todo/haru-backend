package com.haru.backend.task.service;

import com.haru.backend.global.exception.BusinessException;
import com.haru.backend.global.exception.ErrorCode;
import com.haru.backend.task.dto.TaskCreateRequest;
import com.haru.backend.task.dto.TaskOrderRequest;
import com.haru.backend.task.dto.TaskResponse;
import com.haru.backend.task.dto.TaskUpdateRequest;
import com.haru.backend.task.entity.Task;
import com.haru.backend.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional
    public TaskResponse create(UUID userId, TaskCreateRequest request) {
        Task task = Task.create(userId, request.content(), request.taskType());
        taskRepository.save(task);
        return TaskResponse.from(task);
    }

    public List<TaskResponse> getTasks(UUID userId) {
        return taskRepository.findAllByUserIdAndDeletedAtIsNullOrderByDisplayOrderAsc(userId)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional
    public TaskResponse updateContent(UUID userId, Long taskId, TaskUpdateRequest request) {
        Task task = getOwnedTask(userId, taskId);
        task.updateContent(request.content());
        return TaskResponse.from(task);
    }

    @Transactional
    public void delete(UUID userId, Long taskId) {
        Task task = getOwnedTask(userId, taskId);
        task.delete();
    }

    @Transactional
    public TaskResponse changeRecurring(UUID userId, Long taskId, boolean recurring) {
        Task task = getOwnedTask(userId, taskId);
        task.changeRecurring(recurring);
        return TaskResponse.from(task);
    }

    @Transactional
    public void updateOrder(UUID userId, List<TaskOrderRequest.OrderItem> orders) {
        for (TaskOrderRequest.OrderItem item : orders) {
            Task task = getOwnedTask(userId, item.taskId());
            task.changeDisplayOrder(item.displayOrder());
        }
    }

    private Task getOwnedTask(UUID userId, Long taskId) {
        return taskRepository.findByIdAndUserIdAndDeletedAtIsNull(taskId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
    }
}

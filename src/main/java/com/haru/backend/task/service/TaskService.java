package com.haru.backend.task.service;

import com.haru.backend.task.dto.TaskCreateRequest;
import com.haru.backend.task.dto.TaskResponse;
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
}

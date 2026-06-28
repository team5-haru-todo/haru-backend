package com.haru.backend.task.repository;

import com.haru.backend.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByUserIdAndDeletedAtIsNullOrderByDisplayOrderAsc(UUID userId);

    Optional<Task> findByIdAndUserIdAndDeletedAtIsNull(Long id, UUID userId);
}

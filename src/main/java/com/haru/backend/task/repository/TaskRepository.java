package com.haru.backend.task.repository;

import com.haru.backend.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByUserIdAndDeletedAtIsNullOrderByDisplayOrderAsc(UUID userId);

    Optional<Task> findByIdAndUserIdAndDeletedAtIsNull(Long id, UUID userId);

    @Query(value = "SELECT DISTINCT tc.task_id FROM task_completions tc WHERE tc.task_id IN (:taskIds)",
            nativeQuery = true)
    List<Long> findCompletedTaskIds(@Param("taskIds") Collection<Long> taskIds);
}

package com.haru.backend.record.repository;

import com.haru.backend.record.entity.CompletionType;
import com.haru.backend.record.entity.TaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {

    boolean existsByDailyRecordIdAndCompletionType(Long dailyRecordId, CompletionType completionType);

    boolean existsByDailyRecordIdAndTaskId(Long dailyRecordId, Long taskId);

    List<TaskCompletion> findByDailyRecordIdOrderByCompletedAtAsc(Long dailyRecordId);
}

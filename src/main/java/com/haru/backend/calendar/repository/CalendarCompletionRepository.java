package com.haru.backend.calendar.repository;

import com.haru.backend.record.entity.TaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 캘린더에서 보여줄 완료 목록만 읽기 위한 조회용 Repository
public interface CalendarCompletionRepository extends JpaRepository<TaskCompletion, Long> {

    @Query("""
            SELECT completion
            FROM TaskCompletion completion
            WHERE completion.dailyRecordId IN :dailyRecordIds
            ORDER BY completion.dailyRecordId ASC, completion.completedAt ASC
            """)
    List<TaskCompletion> findByDailyRecordIds(@Param("dailyRecordIds") List<Long> dailyRecordIds);
}

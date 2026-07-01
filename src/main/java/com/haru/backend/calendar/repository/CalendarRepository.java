package com.haru.backend.calendar.repository;

import com.haru.backend.record.entity.DailyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// 캘린더는 기록을 새로 만들지 않고, record 도메인에서 만든 일별 기록만 조회한다.
public interface CalendarRepository extends JpaRepository<DailyRecord, Long> {

    List<DailyRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
            UUID userId,
            LocalDate start,
            LocalDate end
    );
}

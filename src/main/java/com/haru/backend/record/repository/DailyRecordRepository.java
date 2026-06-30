package com.haru.backend.record.repository;

import com.haru.backend.record.entity.DailyRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DailyRecordRepository extends JpaRepository<DailyRecord, Long> {

    Optional<DailyRecord> findByUserIdAndRecordDate(UUID userId, LocalDate recordDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT dr FROM DailyRecord dr WHERE dr.userId = :userId AND dr.recordDate = :recordDate")
    Optional<DailyRecord> findWithLockByUserIdAndRecordDate(
            @Param("userId") UUID userId,
            @Param("recordDate") LocalDate recordDate
    );
}

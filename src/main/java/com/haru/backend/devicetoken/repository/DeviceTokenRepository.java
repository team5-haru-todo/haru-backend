package com.haru.backend.devicetoken.repository;

import com.haru.backend.devicetoken.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    //토큰이 없을 수도 있기 때문에 Optional 로 하나 선언을 해놓겠다.
    Optional<DeviceToken> findByToken(String token);

    @Query(value = """
            SELECT dt.token
            FROM device_tokens dt
            JOIN user_settings us ON us.user_id = dt.user_id
            WHERE dt.is_active = TRUE
              AND us.push_enabled = TRUE
              AND NOT EXISTS (
                  SELECT 1
                  FROM daily_records dr
                  WHERE dr.user_id = dt.user_id
                    AND dr.record_date = :date
                    AND dr.current_task_id IS NOT NULL
              )
            """, nativeQuery = true)
    List<String> findActiveTokensWithoutTodayTask(@Param("date") LocalDate date);

    @Query(value = """
            SELECT dt.token
            FROM device_tokens dt
            JOIN user_settings us ON us.user_id = dt.user_id
            WHERE dt.is_active = TRUE
              AND us.push_enabled = TRUE
              AND EXISTS (
                  SELECT 1
                  FROM daily_records dr
                  WHERE dr.user_id = dt.user_id
                    AND dr.record_date = :date
                    AND dr.current_task_id IS NOT NULL
                    AND dr.first_completed_at IS NULL
              )
            """, nativeQuery = true)
    List<String> findActiveTokensWithUncompletedTodayTask(@Param("date") LocalDate date);
}

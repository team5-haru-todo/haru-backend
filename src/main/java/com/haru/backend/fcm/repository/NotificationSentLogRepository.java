package com.haru.backend.fcm.repository;

import com.haru.backend.fcm.entity.NotificationSentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;


public interface NotificationSentLogRepository extends JpaRepository<NotificationSentLog, Long> {
    //중복 체크 token + caseName sentDate로 이미 기록이 있는지 확인
    boolean existsByTokenAndCaseNameAndSentDate(String token, String caseName, LocalDate sentDate);
}

package com.haru.backend.user.repository;

import com.haru.backend.user.entity.WithdrawalLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalLogRepository extends JpaRepository<WithdrawalLog, Long> {
}
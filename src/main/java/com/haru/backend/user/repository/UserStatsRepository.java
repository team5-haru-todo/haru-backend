package com.haru.backend.user.repository;

import com.haru.backend.user.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserStatsRepository extends JpaRepository<UserStats, UUID> {
}
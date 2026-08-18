package com.haru.backend.user.repository;

import com.haru.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // 미사용 게스트 자동 삭제 대상 조회용.
    // status = GUEST 조건이 있어서, ACTIVE 계정은 자동으로 제외됨 (별도 예외 로직 불필요).
    List<User> findAllByStatusAndLastActiveAtBefore(String status, LocalDateTime threshold);
}
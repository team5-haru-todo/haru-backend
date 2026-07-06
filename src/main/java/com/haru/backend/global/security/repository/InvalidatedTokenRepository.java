package com.haru.backend.global.security.repository;

import com.haru.backend.global.security.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

    // 만료 시각이 지난 토큰은 더 이상 블랙리스트로 가지고 있을 필요가 없다 (JWT 자체가 이미 만료돼서 거부되므로).
    // 주기적으로 청소해서 테이블이 무한정 커지는 것을 방지한다.
    void deleteAllByExpiresAtBefore(LocalDateTime dateTime);
}
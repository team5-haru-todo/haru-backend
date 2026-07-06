package com.haru.backend.global.security;

import com.haru.backend.global.security.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvalidatedTokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(InvalidatedTokenCleanupScheduler.class);

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    /**
     * 매일 새벽 4시, 이미 만료된 지 오래된(24시간 이상 지난) 블랙리스트 토큰을 정리한다.
     * 만료된 JWT는 어차피 JwtProvider 파싱 단계에서 거부되므로,
     * 블랙리스트에 계속 남겨둘 필요가 없어 테이블이 무한정 커지는 것을 방지한다.
     */
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        invalidatedTokenRepository.deleteAllByExpiresAtBefore(cutoff);
        log.info("만료된 블랙리스트 토큰 정리 완료. cutoff={}", cutoff);
    }
}

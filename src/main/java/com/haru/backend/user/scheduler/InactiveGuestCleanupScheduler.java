package com.haru.backend.user.scheduler;

import com.haru.backend.user.entity.User;
import com.haru.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InactiveGuestCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(InactiveGuestCleanupScheduler.class);

    // 미사용 게스트 삭제 기준. Refresh Token 유효기간과 동일하게 40일로 맞춘다.
    private static final long INACTIVE_THRESHOLD_DAYS = 40;

    private final UserRepository userRepository;

    /**
     * 매일 새벽 4시 30분, 40일 넘게 활동이 없는 GUEST 계정을 삭제한다.
     *
     * User 삭제 시 tasks/daily_records(및 그 안의 task_completions)/device_tokens/
     * refresh_tokens가 전부 ON DELETE CASCADE로 함께 삭제된다 (별도 삭제 호출 불필요).
     *
     * status = GUEST 조건이 있어서 ACTIVE 계정은 자동으로 제외된다.
     * 탈퇴(withdraw)와 달리 사유를 남길 필요가 없는 흐름이라, withdrawal_log는 남기지 않는다.
     */
    @Scheduled(cron = "0 30 4 * * *", zone = "Asia/Seoul")
    @Transactional
    public void cleanupInactiveGuests() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(INACTIVE_THRESHOLD_DAYS);
        List<User> inactiveGuests = userRepository.findAllByStatusAndLastActiveAtBefore("GUEST", threshold);

        if (inactiveGuests.isEmpty()) {
            log.info("미사용 게스트 정리 - 삭제 대상 없음. threshold={}", threshold);
            return;
        }

        userRepository.deleteAll(inactiveGuests);
        log.info("미사용 게스트 정리 완료. 삭제된 계정 수={}, threshold={}", inactiveGuests.size(), threshold);
    }
}

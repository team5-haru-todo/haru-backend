package com.haru.backend.devicetoken.service;

import com.haru.backend.devicetoken.repository.DeviceTokenRepository;
import com.haru.backend.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceTokenReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeviceTokenReminderScheduler.class);
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final DeviceTokenRepository deviceTokenRepository;
    private final FcmService fcmService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendMorningReminder() {
        LocalDate today = LocalDate.now(SEOUL);

        // 오전 9시: 아직 '오늘의 한 개'를 정하지 않은 사용자.
        List<String> tokens = deviceTokenRepository.findActiveTokensWithoutTodayTask(today);
        sendRandom(tokens, ReminderMessages.MORNING_NO_TASK, "morning-no-task");
    }

    @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Seoul")
    public void sendEveningReminder() {
        LocalDate today = LocalDate.now(SEOUL);

        // 오후 9시 ①: 오늘 '오늘의 한 개'를 정하지 않은 사용자.
        List<String> unselectedTokens = deviceTokenRepository.findActiveTokensWithoutTodayTask(today);
        sendRandom(unselectedTokens, ReminderMessages.EVENING_NO_TASK, "evening-no-task");

        // 오후 9시 ②: '오늘의 한 개'를 정했지만 아직 완료하지 않은 사용자.
        List<String> uncompletedTokens = deviceTokenRepository.findActiveTokensWithUncompletedTodayTask(today);
        sendRandom(uncompletedTokens, ReminderMessages.EVENING_UNCOMPLETED, "evening-uncompleted");

        // 오후 9시 ③: '오늘의 한 개'는 완료했지만 추가로 완료하지 않은 할 일이 남아있는 사용자.
        List<String> remainingTokens = deviceTokenRepository.findActiveTokensWithCompletedButRemainingTasks(today);
        sendRandom(remainingTokens, ReminderMessages.EVENING_COMPLETED_WITH_REMAINING, "evening-completed-remaining");
    }

    /**
     * 대상 토큰마다 문구 목록에서 하나를 랜덤으로 골라 발송한다.
     */
    private void sendRandom(List<String> tokens, List<ReminderMessage> pool, String caseName) {
        if (tokens.isEmpty()) {
            log.info("발송할 디바이스 토큰이 없습니다. case={}", caseName);
            return;
        }

        for (String token : tokens) {
            ReminderMessage message = ReminderMessages.pickRandom(pool);
            fcmService.sendMessage(caseName,token, message.title(), message.body());
        }
        log.info("예약 푸시 발송 완료. case={}, count={}", caseName, tokens.size());
    }
}

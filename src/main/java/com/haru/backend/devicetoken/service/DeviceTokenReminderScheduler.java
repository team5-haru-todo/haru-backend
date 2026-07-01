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

        // 오전 9시에는 아직 오늘의 한 개를 고르지 않은 사용자에게만 보낸다.
        List<String> tokens = deviceTokenRepository.findActiveTokensWithoutTodayTask(today);
        sendToTokens(
                tokens,
                "오늘의 한 개를 정해볼까요?",
                "작은 일 하나만 골라도 충분해요."
        );
    }

    @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Seoul")
    public void sendEveningReminder() {
        LocalDate today = LocalDate.now(SEOUL);

        // 오후 9시에는 오늘의 한 개를 아직 고르지 않은 사용자에게 한 번 더 알려준다.
        List<String> unselectedTokens = deviceTokenRepository.findActiveTokensWithoutTodayTask(today);
        sendToTokens(
                unselectedTokens,
                "오늘의 한 개를 아직 정하지 않았어요",
                "늦지 않았어요. 지금 하나만 정해볼까요?"
        );

        // 오늘의 한 개를 골랐지만 완료하지 않은 사용자는 다른 문구로 마무리를 유도한다.
        List<String> uncompletedTokens = deviceTokenRepository.findActiveTokensWithUncompletedTodayTask(today);
        sendToTokens(
                uncompletedTokens,
                "오늘의 한 개를 완료해볼까요?",
                "작게라도 끝내면 오늘의 불꽃을 받을 수 있어요."
        );
    }

    private void sendToTokens(List<String> tokens, String title, String body) {
        if (tokens.isEmpty()) {
            log.info("발송할 디바이스 토큰이 없습니다. title={}", title);
            return;
        }

        for (String token : tokens) {
            fcmService.sendMessage(token, title, body);
        }
        log.info("예약 푸시 발송 완료. title={}, count={}", title, tokens.size());
    }
}

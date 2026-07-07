package com.haru.backend.webhook.controller;

import com.haru.backend.webhook.service.SocialUnlinkWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class SocialUnlinkWebhookController {

    private final SocialUnlinkWebhookService socialUnlinkWebhookService;

    /**
     * 카카오 "연결 끊기" 웹훅.
     * 카카오는 이 요청을 application/x-www-form-urlencoded 형식으로 보낸다 (JSON 아님).
     * 예: app_id=1497000&user_id=4962821795&referrer_type=ACCOUNT_DELETE
     * 참고: https://developers.kakao.com/docs/latest/ko/kakaologin/unlink-webhook
     */
    @PostMapping("/kakao/unlink")
    public ResponseEntity<Void> handleKakaoUnlink(
            @RequestParam("user_id") String userId,
            @RequestParam("referrer_type") String referrerType,
            @RequestParam(value = "app_id", required = false) String appId
    ) {
        log.info("카카오 연결 끊기 웹훅 수신 - userId: {}, referrerType: {}, appId: {}",
                userId, referrerType, appId);
        socialUnlinkWebhookService.handleUnlink("KAKAO", userId);
        return ResponseEntity.ok().build();
    }
}
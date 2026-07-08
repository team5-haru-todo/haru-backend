package com.haru.backend.webhook.controller;

import com.haru.backend.webhook.dto.AppleWebhookRequest;
import com.haru.backend.webhook.service.AppleWebhookService;
import com.haru.backend.webhook.service.SocialUnlinkWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class SocialUnlinkWebhookController {

    private final SocialUnlinkWebhookService socialUnlinkWebhookService;
    private final AppleWebhookService appleWebhookService;

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

    /**
     * Apple Server-to-Server Notification.
     * Apple은 JSON body로 { "payload": "<JWT>" } 형태로 보낸다.
     * payload(JWT) 안에 실제 이벤트 정보(연결 해제, 계정 삭제 등)가 서명된 채로 담겨 있다.
     * 참고: https://developer.apple.com/documentation/sign_in_with_apple/processing_changes_for_sign_in_with_apple_accounts
     */
    @PostMapping("/apple/notification")
    public ResponseEntity<Void> handleAppleNotification(@RequestBody AppleWebhookRequest request) {
        log.info("Apple 웹훅 수신");
        appleWebhookService.handleNotification(request.payload());
        return ResponseEntity.ok().build();
    }
}
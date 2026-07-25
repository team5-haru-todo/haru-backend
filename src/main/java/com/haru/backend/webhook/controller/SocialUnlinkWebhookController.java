package com.haru.backend.webhook.controller;

import com.haru.backend.webhook.dto.AppleWebhookRequest;
import com.haru.backend.webhook.service.AppleWebhookService;
import com.haru.backend.webhook.service.SocialUnlinkWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class SocialUnlinkWebhookController {

    private final SocialUnlinkWebhookService socialUnlinkWebhookService;
    private final AppleWebhookService appleWebhookService;

    // 카카오는 이 웹훅에 서명(JWT) 방식을 제공하지 않는다.
    // 대신 카카오 개발자 콘솔에 등록하는 콜백 URL 자체에 시크릿 토큰을 쿼리 파라미터로 심어두고,
    // 요청마다 그 값이 일치하는지 검증하는 방식으로 위조 요청을 막는다.
    // 예: https://<host>/api/webhooks/kakao/unlink?token=<KAKAO_WEBHOOK_TOKEN>
    @Value("${kakao.webhook.token}")
    private String kakaoWebhookToken;

    /**
     * 카카오 "연결 끊기" 웹훅.
     * 카카오는 이 요청을 application/x-www-form-urlencoded 형식으로 보낸다 (JSON 아님).
     * 예: app_id=1497000&user_id=4962821795&referrer_type=ACCOUNT_DELETE
     * 참고: https://developers.kakao.com/docs/latest/ko/kakaologin/unlink-webhook
     *
     * 이 엔드포인트는 SecurityConfig에서 permitAll로 열려 있어 JWT 인증이 없다.
     * 대신 콜백 URL에 심어둔 token 쿼리 파라미터로 요청 출처를 검증한다 — 실제 카카오가
     * 등록된 콜백 URL로 보낸 요청이 아니면 token이 없거나 값이 다르므로 401로 거부된다.
     */
    @PostMapping("/kakao/unlink")
    public ResponseEntity<Void> handleKakaoUnlink(
            @RequestParam("user_id") String userId,
            @RequestParam("referrer_type") String referrerType,
            @RequestParam(value = "app_id", required = false) String appId,
            @RequestParam(value = "token", required = false) String token
    ) {
        if (!isValidToken(token)) {
            log.warn("카카오 웹훅 - 시크릿 토큰 불일치. 위조되었거나 잘못 설정된 요청일 수 있음. userId: {}", userId);
            return ResponseEntity.status(401).build();
        }

        log.info("카카오 연결 끊기 웹훅 수신 - userId: {}, referrerType: {}, appId: {}",
                userId, referrerType, appId);
        socialUnlinkWebhookService.handleUnlink("KAKAO", userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Apple Server-to-Server Notification.
     * Apple은 JSON body로 { "payload": "<JWT>" } 형태로 보낸다.
     * payload(JWT) 안에 실제 이벤트 정보(연결 해제, 계정 삭제 등)가 서명된 채로 담겨 있다.
     * AppleWebhookService에서 Apple 공개키로 서명을 검증하므로 위조된 요청은 여기서 걸러진다.
     * 참고: https://developer.apple.com/documentation/sign_in_with_apple/processing_changes_for_sign_in_with_apple_accounts
     */
    @PostMapping("/apple/notification")
    public ResponseEntity<Void> handleAppleNotification(@RequestBody AppleWebhookRequest request) {
        log.info("Apple 웹훅 수신");
        appleWebhookService.handleNotification(request.payload());
        return ResponseEntity.ok().build();
    }

    // 타이밍 공격을 방지하기 위해 문자열 길이에 따라 실행 시간이 달라지지 않는 상수 시간 비교를 사용한다.
    private boolean isValidToken(String token) {
        if (token == null || kakaoWebhookToken == null || kakaoWebhookToken.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8),
                kakaoWebhookToken.getBytes(StandardCharsets.UTF_8)
        );
    }
}
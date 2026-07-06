package com.haru.backend.webhook.controller;

import com.haru.backend.webhook.dto.KakaoUnlinkWebhookRequest;
import com.haru.backend.webhook.service.SocialUnlinkWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class SocialUnlinkWebhookController {

    private final SocialUnlinkWebhookService socialUnlinkWebhookService;

    /**
     * 카카오 "연결 끊기" 웹훅.
     * 카카오 개발자 콘솔 > 카카오 로그인 > 보안 > Unlink Callback 에 이 URL을 등록해야 실제로 호출됨.
     * 참고: https://developers.kakao.com/docs/latest/ko/kakaologin/unlink-webhook
     */
    @PostMapping("/kakao/unlink")
    public ResponseEntity<Void> handleKakaoUnlink(@RequestBody KakaoUnlinkWebhookRequest request) {
        log.info("카카오 연결 끊기 웹훅 수신 - userId: {}, referrerType: {}",
                request.userId(), request.referrerType());
        socialUnlinkWebhookService.handleUnlink("KAKAO", request.userId());
        return ResponseEntity.ok().build();
    }
}

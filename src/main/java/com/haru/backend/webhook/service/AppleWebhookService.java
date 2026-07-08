package com.haru.backend.webhook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haru.backend.auth.client.AppleTokenVerifier;
import com.haru.backend.webhook.dto.AppleEvent;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleWebhookService {

    private static final String EVENT_CONSENT_REVOKED = "consent-revoked";
    private static final String EVENT_ACCOUNT_DELETE = "account-delete";

    private final AppleTokenVerifier appleTokenVerifier;
    private final SocialUnlinkWebhookService socialUnlinkWebhookService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Apple Server-to-Server Notification 처리.
     * payload(JWT)를 Apple 공개키로 검증한 뒤, 그 안의 events 클레임을 파싱해서
     * 연결 해제(consent-revoked) 또는 계정 삭제(account-delete) 이벤트면 SocialAccount를 정리한다.
     */
    public void handleNotification(String payload) {
        JWTClaimsSet claims = appleTokenVerifier.verifyAndGetClaims(payload);

        try {
            String eventsJson = claims.getStringClaim("events");
            if (eventsJson == null) {
                log.warn("Apple 웹훅 - events 클레임이 없습니다.");
                return;
            }

            AppleEvent event = objectMapper.readValue(eventsJson, AppleEvent.class);
            log.info("Apple 웹훅 이벤트 수신 - type: {}, sub: {}", event.type(), event.sub());

            if (EVENT_CONSENT_REVOKED.equals(event.type()) || EVENT_ACCOUNT_DELETE.equals(event.type())) {
                socialUnlinkWebhookService.handleUnlink("APPLE", event.sub());
            }
        } catch (Exception e) {
            log.error("Apple 웹훅 이벤트 파싱 실패", e);
        }
    }
}

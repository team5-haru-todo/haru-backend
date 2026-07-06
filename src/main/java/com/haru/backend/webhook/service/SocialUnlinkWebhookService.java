package com.haru.backend.webhook.service;

import com.haru.backend.user.entity.SocialAccount;
import com.haru.backend.user.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialUnlinkWebhookService {

    private final SocialAccountRepository socialAccountRepository;

    /**
     * 카카오/Apple에서 "사용자가 직접 연결 끊기"를 했을 때 오는 웹훅 처리.
     * 우리 서비스 DB에서 해당 SocialAccount 연결 정보를 삭제한다.
     * (User 본체는 삭제하지 않음 — 유저는 게스트로 전환되거나 다른 로그인 수단이 남아있을 수 있음)
     */
    @Transactional
    public void handleUnlink(String provider, String providerUserId) {
        socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .ifPresentOrElse(
                        socialAccount -> {
                            log.info("소셜 연결 끊기 웹훅 처리 - provider: {}, providerUserId: {}, userId: {}",
                                    provider, providerUserId, socialAccount.getUser().getId());
                            socialAccountRepository.delete(socialAccount);
                        },
                        () -> log.warn("소셜 연결 끊기 웹훅 - 해당하는 SocialAccount를 찾을 수 없음. provider: {}, providerUserId: {}",
                                provider, providerUserId)
                );
    }
}

package com.haru.backend.auth.token;

import com.haru.backend.global.exception.BusinessException;
import com.haru.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class WidgetTokenService {
    private static final long VALIDITY_DAYS = 40;
    private final WidgetTokenRepository tokenRepository;
    private final SecureRandom secureRandom;
    private final WidgetTokenReuseHandler reuseHandler;

// DB 건드리는 작업 (최초 발급)
@Transactional
public String issue(UUID userId) {
    String rawToken = generateRawToken();
    String hash = hash(rawToken);
    Instant expiresAt = Instant.now().plus(VALIDITY_DAYS, ChronoUnit.DAYS);
    WidgetToken token = WidgetToken.issue(userId, null, expiresAt, hash);
    tokenRepository.save(token);
    token.assignFamilyId(token.getId());
    return  rawToken;
}
//재사용 탐지 코드
@Transactional
public String rotate(String rawToken) {
    String hash = hash(rawToken);
    WidgetToken token = tokenRepository.findByTokenHash(hash)
            .orElseThrow(() -> new BusinessException(ErrorCode.WIDGET_TOKEN_NOT_FOUND));

    // 1. 만료 체크
    if (token.isExpired()) {
        throw new BusinessException(ErrorCode.WIDGET_TOKEN_EXPIRED);
    }

    // 2. 재사용 탐지: ACTIVE가 아니면(ROTATED/REVOKED) → family 전체 폐기
    if (token.getStatus() != WidgetTokenStatus.ACTIVE) {
        reuseHandler.revokeFamily(token.getFamilyId());   // 별도 트랜잭션에서 폐기 (먼저 커밋됨)
        throw new BusinessException(ErrorCode.WIDGET_TOKEN_REUSED);   // 그 다음 바깥 롤백
    }

    // 3. 정상 회전
    token.markRotated();                       // 기존 토큰 회전 처리
    String newRaw = generateRawToken();
    WidgetToken newToken = WidgetToken.issue(   // ← getUserId/getFamilyId를 '인자로' 넣음
            token.getUserId(),
            token.getFamilyId(),                // 부모 familyId 물려받음 (null 아님!)
            Instant.now().plus(VALIDITY_DAYS, ChronoUnit.DAYS),
            hash(newRaw));
    tokenRepository.save(newToken);
    return newRaw;                              // 새 열쇠 반환
}
private String generateRawToken(){
    byte[] bytes = new byte[16];
    secureRandom.nextBytes(bytes);
    return Base64.getEncoder().encodeToString(bytes);
}
private String hash(String rawToken){
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return Base64.getEncoder().encodeToString(digest.digest(rawToken.getBytes()));
    } catch (NoSuchAlgorithmException e) {
        throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
    }

}

}

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

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    // 미사용 게스트 삭제 기준(40일)과 동일하게 맞춘다.
    private static final long VALIDITY_DAYS = 40;

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 새 Refresh Token을 발급한다.
     * 이미 해당 유저의 토큰이 있으면 지우고 새로 발급한다(로그인/게스트생성 시 항상 최신 상태 하나만 유지).
     *
     * @return 클라이언트에 내려줄 토큰 원문 (이 문자열은 DB에 저장되지 않음)
     */
    @Transactional
    public String issue(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
        refreshTokenRepository.flush();

        String rawToken = generateRawToken();
        String tokenHash = hash(rawToken);
        Instant expiresAt = Instant.now().plus(VALIDITY_DAYS, ChronoUnit.DAYS);

        refreshTokenRepository.save(RefreshToken.create(userId, tokenHash, expiresAt));

        return rawToken;
    }

    /**
     * Refresh Token을 검증하고, 통과하면 로테이션(기존 삭제 + 새 토큰 발급)한다.
     *
     * @return 새로 발급된 토큰 원문과 userId
     */
    @Transactional
    public ReissueResult reissue(String rawToken) {
        String tokenHash = hash(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        UUID userId = refreshToken.getUserId();
        String newRawToken = issue(userId); // 내부에서 기존 것 삭제 후 재발급 (로테이션)

        return new ReissueResult(userId, newRawToken);
    }

    @Transactional
    public void revoke(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
        refreshTokenRepository.flush();
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    public record ReissueResult(UUID userId, String newRawToken) {}
}

package com.haru.backend.global.security;

import com.haru.backend.global.security.entity.InvalidatedToken;
import com.haru.backend.global.security.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class TokenInvalidationService {

    private final JwtProvider jwtProvider;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    /**
     * 로그아웃/탈퇴 시 현재 사용 중인 토큰을 블랙리스트에 등록한다.
     * 이후 이 토큰으로 오는 요청은 JwtAuthenticationFilter에서 거부된다.
     */
    @Transactional
    public void invalidate(String token) {
        String tokenId = jwtProvider.getTokenId(token);
        if (tokenId == null) {
            return;
        }
        LocalDateTime expiresAt = jwtProvider.getExpiration(token)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        if (!invalidatedTokenRepository.existsById(tokenId)) {
            invalidatedTokenRepository.save(new InvalidatedToken(tokenId, expiresAt));
        }
    }
}

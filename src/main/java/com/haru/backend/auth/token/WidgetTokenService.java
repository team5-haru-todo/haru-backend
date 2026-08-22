package com.haru.backend.auth.token;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
//최초 발급
@Transactional
public String issue(UUID userId){
    String rawToken = generateRawToken();
    String hash = hash(rawToken);
    Instant expiredAt = Instant.now().plus(VALIDITY_DAYS, ChronoUnit.DAYS);


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

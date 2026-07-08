package com.haru.backend.auth.client;

import com.haru.backend.global.exception.BusinessException;
import com.haru.backend.global.exception.ErrorCode;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Date;

// Apple이 발급한 identityToken(JWT)이 실제로 Apple이 서명한 유효한 토큰인지 검증한다.
// Apple 공개키(https://appleid.apple.com/auth/keys)로 서명을 확인하고,
// 발급자(issuer)/대상(audience)/만료시간까지 검증한 뒤 유저 고유 식별자(sub)를 반환한다.
@Component
public class AppleTokenVerifier {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";

    @Value("${apple.bundle-id}")
    private String appleBundleId;

    public String verifyAndGetSubject(String identityToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(identityToken);
            String kid = signedJWT.getHeader().getKeyID();

            JWKSet jwkSet = JWKSet.load(new URL(APPLE_KEYS_URL));
            JWK jwk = jwkSet.getKeyByKeyId(kid);
            if (jwk == null) {
                throw new BusinessException(ErrorCode.INVALID_APPLE_TOKEN);
            }

            RSAKey rsaKey = jwk.toRSAKey();
            JWSVerifier verifier = new RSASSAVerifier(rsaKey);
            if (!signedJWT.verify(verifier)) {
                throw new BusinessException(ErrorCode.INVALID_APPLE_TOKEN);
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (!APPLE_ISSUER.equals(claims.getIssuer())) {
                throw new BusinessException(ErrorCode.INVALID_APPLE_TOKEN);
            }

            if (claims.getAudience() == null || !claims.getAudience().contains(appleBundleId)) {
                throw new BusinessException(ErrorCode.INVALID_APPLE_TOKEN);
            }

            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.before(new Date())) {
                throw new BusinessException(ErrorCode.INVALID_APPLE_TOKEN);
            }

            return claims.getSubject();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_APPLE_TOKEN);
        }
    }

    /**
     * Server-to-Server Notification의 payload(JWT)를 검증하고 클레임 전체를 반환한다.
     * 일반 identityToken 검증과 달리 audience 체크는 하지 않는다
     * (Apple 웹훅 payload의 클레임 구조는 로그인용 identityToken과 다르다).
     */
    public JWTClaimsSet verifyAndGetClaims(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String kid = signedJWT.getHeader().getKeyID();

            JWKSet jwkSet = JWKSet.load(new URL(APPLE_KEYS_URL));
            JWK jwk = jwkSet.getKeyByKeyId(kid);
            if (jwk == null) {
                throw new BusinessException(ErrorCode.INVALID_APPLE_TOKEN);
            }

            RSAKey rsaKey = jwk.toRSAKey();
            JWSVerifier verifier = new RSASSAVerifier(rsaKey);
            if (!signedJWT.verify(verifier)) {
                throw new BusinessException(ErrorCode.INVALID_APPLE_TOKEN);
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (!APPLE_ISSUER.equals(claims.getIssuer())) {
                throw new BusinessException(ErrorCode.INVALID_APPLE_TOKEN);
            }

            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.before(new Date())) {
                throw new BusinessException(ErrorCode.INVALID_APPLE_TOKEN);
            }

            return claims;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_APPLE_TOKEN);
        }
    }
}
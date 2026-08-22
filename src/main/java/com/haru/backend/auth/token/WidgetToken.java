package com.haru.backend.auth.token;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "widget_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class WidgetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long familyId;

    //enum을 DB에 문자열로 저장
    @Enumerated(EnumType.STRING)
    @Column(name= "status")
    private WidgetTokenStatus status;

    @Column(nullable = false)
    private String tokenHash;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt;

    private WidgetToken(UUID userId, Long familyId, Instant expiresAt,String tokenHash) {
        this.userId = userId;
        this.familyId = familyId;
        this.expiresAt = expiresAt;
        this.tokenHash = tokenHash;
        this.createdAt = Instant.now();
        this.status = WidgetTokenStatus.ACTIVE;
    }
    public void markRotated() {
        this.status = WidgetTokenStatus.ROTATED;
    }
    public void revoke() {
        this.status = WidgetTokenStatus.REVOKED;
    }
    public void assignFamilyId(Long familyId) {
        this.familyId = familyId;
    }
    public static WidgetToken issue(UUID userId, Long familyId, Instant expiresAt,String tokenHash) {
        WidgetToken token = new WidgetToken(userId, familyId, expiresAt,tokenHash);
        return token;
    }
}

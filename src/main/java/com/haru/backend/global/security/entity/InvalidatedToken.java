package com.haru.backend.global.security.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "invalidated_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvalidatedToken {

    @Id
    private String tokenId;

    private LocalDateTime expiresAt;

    private LocalDateTime invalidatedAt;

    public InvalidatedToken(String tokenId, LocalDateTime expiresAt) {
        this.tokenId = tokenId;
        this.expiresAt = expiresAt;
        this.invalidatedAt = LocalDateTime.now();
    }
}

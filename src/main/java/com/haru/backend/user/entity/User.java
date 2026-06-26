package com.haru.backend.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "terms_version", length = 20)
    private String termsVersion;

    @Column(name = "terms_agreed_at")
    private LocalDateTime termsAgreedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    public static User createGuest() {
        User user = new User();
        user.status = "GUEST";
        user.nickname = "게스트";
        return user;
    }

    public static User createActive(String nickname) {
        User user = new User();
        user.status = "ACTIVE";
        user.nickname = nickname;
        return user;
    }

    public static User createFromSocial(String nickname, String termsVersion, LocalDateTime termsAgreedAt) {
        User user = new User();
        user.status = "ACTIVE";
        user.nickname = nickname;
        user.termsVersion = termsVersion;
        user.termsAgreedAt = termsAgreedAt;
        return user;
    }

    public void activate(String termsVersion, LocalDateTime termsAgreedAt) {
        this.status = "ACTIVE";
        this.termsVersion = termsVersion;
        this.termsAgreedAt = termsAgreedAt;
    }

    public void agreeToTerms(String termsVersion, LocalDateTime termsAgreedAt) {
        this.termsVersion = termsVersion;
        this.termsAgreedAt = termsAgreedAt;
    }
}
package com.haru.backend.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "withdrawal_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "etc_reason", columnDefinition = "TEXT")
    private String etcReason;

    @Column(name = "withdrawn_at", nullable = false)
    private LocalDateTime withdrawnAt;

    @OneToMany(mappedBy = "withdrawalLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WithdrawalReason> reasons = new ArrayList<>();

    @Builder
    public WithdrawalLog(UUID userId, String etcReason, LocalDateTime withdrawnAt) {
        this.userId = userId;
        this.etcReason = etcReason;
        this.withdrawnAt = withdrawnAt;
    }

    public void addReason(String reason) {
        WithdrawalReason withdrawalReason = new WithdrawalReason(this, reason);
        this.reasons.add(withdrawalReason);
    }
}
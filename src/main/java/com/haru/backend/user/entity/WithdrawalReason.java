package com.haru.backend.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "withdrawal_reason")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "withdrawal_log_id", nullable = false)
    private WithdrawalLog withdrawalLog;

    @Column(name = "reason", nullable = false, length = 100)
    private String reason;

    WithdrawalReason(WithdrawalLog withdrawalLog, String reason) {
        this.withdrawalLog = withdrawalLog;
        this.reason = reason;
    }
}
package com.haru.backend.fcm.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_sent_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSentLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="token",nullable=false)
    private String token;

    @Column(name="case_name",nullable=false)
    private String caseName;

    @Column(name="sent_at", nullable=false)
    private LocalDateTime sentAt;

    @Column(name="sent_date",nullable=false)
    private LocalDate sentDate;

    @Builder
    public NotificationSentLog(String token, String caseName, LocalDateTime sentAt, LocalDate sentDate) {
        this.token = token;
        this.caseName = caseName;
        this.sentAt = LocalDateTime.now();
        this.sentDate =  sentDate;
    }
}

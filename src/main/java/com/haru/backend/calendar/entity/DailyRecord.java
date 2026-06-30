package com.haru.backend.calendar.entity;

import com.haru.backend.task.entity.Task;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name="daily_records")
public class DailyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false, updatable = false)
    private Long id;

    //insert 때만 값을 넣도록 설정
    @Column(name="user_id",nullable = false,updatable = false)
    //@ManyToOne 관계를 설정하면, 연결된 객체를 바로 꺼내 쓸 수 있게 된다.
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    //대표 task 관계로 연결
    @JoinColumn(name = "current_task_id")
    private Task currentTask;

    @Column(name="record_date",nullable = false)
    private LocalDate recordDate;

    @CreatedDate
    @Column(name="created_at")
    //Instant 타입 : 전 세계 어디서든 동일한 바로 그 순간(UTC)
    private Instant createdAt;
    @LastModifiedDate
    @Column(name="updated_at", nullable = false)
    private Instant updatedAt;


    //아직 완료하지 않았을 수 있기 때문에 nullalbe
    @Column(name="first_completed_at")
    private Instant firstCompletedAt;
    @Column(name ="fire_earned",nullable = false,updatable = false)
    private boolean fireEarned = false;

}

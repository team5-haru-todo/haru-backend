package com.haru.backend.record.service;

import com.haru.backend.global.exception.BusinessException;
import com.haru.backend.global.exception.ErrorCode;
import com.haru.backend.record.dto.*;
import com.haru.backend.record.entity.CompletionType;
import com.haru.backend.record.entity.DailyRecord;
import com.haru.backend.record.entity.TaskCompletion;
import com.haru.backend.record.repository.DailyRecordRepository;
import com.haru.backend.record.repository.TaskCompletionRepository;
import com.haru.backend.user.entity.UserStats;
import com.haru.backend.user.repository.UserStatsRepository;
import com.haru.backend.task.entity.Task;
import com.haru.backend.task.entity.TaskType;
import com.haru.backend.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final DailyRecordRepository dailyRecordRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final UserStatsRepository userStatsRepository;
    // cross-domain: record domain uses task domain's repository for task lookup and creation
    private final TaskRepository taskRepository;

    // ── GET /api/today ────────────────────────────────────────────────────────

    public TodayResponse getToday(UUID userId) {
        LocalDate today = today();
        Optional<DailyRecord> recordOpt = dailyRecordRepository.findByUserIdAndRecordDate(userId, today);

        if (recordOpt.isEmpty()) {
            return emptyTodayResponse(today);
        }

        DailyRecord record = recordOpt.get();
        Task currentTask = loadCurrentTask(userId, record);
        List<CompletionResponse> completedTasks = loadCompletions(record.getId());

        boolean hasFirst = record.hasFirstCompletion();
        boolean canFirstComplete = currentTask != null && !currentTask.isDeleted() && !hasFirst;
        boolean canAdditionalComplete = hasFirst;

        return new TodayResponse(
                today,
                currentTask != null ? CurrentTaskResponse.from(currentTask) : null,
                record.isFireEarned(),
                toSeoul(record.getFirstCompletedAt()),
                canFirstComplete,
                canAdditionalComplete,
                completedTasks
        );
    }

    // ── POST /api/today/task ──────────────────────────────────────────────────

    @Transactional
    public TodayTaskSetResponse createTodayTask(UUID userId, CreateTodayTaskRequest request) {
        // task 도메인 팩토리 메서드 + 레포지토리 직접 사용 (TaskService.create()와 동일 정책: displayOrder=0, null→GENERAL)
        TaskType taskType = request.taskType() == null ? TaskType.GENERAL : request.taskType();
        Task task = Task.create(userId, request.content(), taskType);
        taskRepository.save(task);

        LocalDate today = today();
        Instant now = Instant.now();
        DailyRecord record = findOrCreateRecord(userId, today);
        record.assignTask(task.getId(), now);

        return new TodayTaskSetResponse(
                today,
                CurrentTaskResponse.from(task),
                toSeoul(now)
        );
    }

    // ── PATCH /api/today/task ─────────────────────────────────────────────────

    @Transactional
    public TodayTaskSetResponse setTodayTask(UUID userId, SetTodayTaskRequest request) {
        // findByIdAndUserIdAndDeletedAtIsNull: 미존재·미소유·삭제 모두 TASK_NOT_FOUND (task PR 기준)
        Task task = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(request.taskId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        LocalDate today = today();
        Instant now = Instant.now();
        DailyRecord record = findOrCreateRecord(userId, today);
        record.assignTask(task.getId(), now);

        return new TodayTaskSetResponse(
                today,
                CurrentTaskResponse.from(task),
                toSeoul(now)
        );
    }

    // ── DELETE /api/today/task ────────────────────────────────────────────────

    @Transactional
    public void clearTodayTask(UUID userId) {
        LocalDate today = today();
        dailyRecordRepository.findByUserIdAndRecordDate(userId, today)
                .ifPresent(DailyRecord::clearTask);
    }

    // ── POST /api/today/complete ──────────────────────────────────────────────

    @Transactional
    public FirstCompleteResponse completeFirst(UUID userId) {
        LocalDate today = today();
        Instant now = Instant.now();

        // pessimistic lock으로 동시 중복 완료 방지, DB unique partial index가 2차 방어선
        // record 없음은 사용자 관점에서 오늘의 한 개 미설정 상태와 동일
        DailyRecord record = dailyRecordRepository.findWithLockByUserIdAndRecordDate(userId, today)
                .orElseThrow(() -> new BusinessException(ErrorCode.TODAY_TASK_NOT_SELECTED));

        if (record.getCurrentTaskId() == null) {
            throw new BusinessException(ErrorCode.TODAY_TASK_NOT_SELECTED);
        }
        if (record.hasFirstCompletion()) {
            throw new BusinessException(ErrorCode.ALREADY_COMPLETED_TODAY);
        }

        // 미존재·미소유·삭제 모두 TASK_NOT_FOUND (task PR 기준)
        Task task = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(record.getCurrentTaskId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        if (taskCompletionRepository.existsByDailyRecordIdAndTaskId(record.getId(), task.getId())) {
            throw new BusinessException(ErrorCode.TASK_ALREADY_COMPLETED_TODAY);
        }

        TaskCompletion completion = taskCompletionRepository.save(
                TaskCompletion.create(record, task, CompletionType.FIRST, now)
        );
        record.recordFirstCompletion(now);

        UserStats stats = userStatsRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATS_NOT_FOUND));
        stats.applyFirstCompletion(today);

        return new FirstCompleteResponse(
                today,
                CompletionResponse.from(completion),
                record.isFireEarned(),
                StreakResponse.from(stats)
        );
    }

    // ── POST /api/today/additional-complete ───────────────────────────────────

    @Transactional
    public AdditionalCompleteResponse completeAdditional(UUID userId, AdditionalCompleteRequest request) {
        LocalDate today = today();
        Instant now = Instant.now();

        // record 없음도 첫 완료 전 상태와 동일하게 처리
        DailyRecord record = dailyRecordRepository.findByUserIdAndRecordDate(userId, today)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDITIONAL_COMPLETION_BEFORE_FIRST));

        if (!record.hasFirstCompletion()) {
            throw new BusinessException(ErrorCode.ADDITIONAL_COMPLETION_BEFORE_FIRST);
        }

        Task task = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(request.taskId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        if (taskCompletionRepository.existsByDailyRecordIdAndTaskId(record.getId(), task.getId())) {
            throw new BusinessException(ErrorCode.TASK_ALREADY_COMPLETED_TODAY);
        }

        TaskCompletion completion = taskCompletionRepository.save(
                TaskCompletion.create(record, task, CompletionType.ADDITIONAL, now)
        );

        return new AdditionalCompleteResponse(today, CompletionResponse.from(completion));
    }

    // ── GET /api/streak ───────────────────────────────────────────────────────

    public StreakResponse getStreak(UUID userId) {
        return userStatsRepository.findById(userId)
                .map(StreakResponse::from)
                .orElse(StreakResponse.empty());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private LocalDate today() {
        return LocalDate.now(SEOUL);
    }

    private DailyRecord findOrCreateRecord(UUID userId, LocalDate date) {
        return dailyRecordRepository.findByUserIdAndRecordDate(userId, date)
                .orElseGet(() -> dailyRecordRepository.save(DailyRecord.create(userId, date)));
    }

    private Task loadCurrentTask(UUID userId, DailyRecord record) {
        if (record.getCurrentTaskId() == null) {
            return null;
        }
        // task 도메인 조회 기준과 일치: 미존재·미소유·삭제 task는 null로 처리
        return taskRepository.findByIdAndUserIdAndDeletedAtIsNull(record.getCurrentTaskId(), userId)
                .orElse(null);
    }

    private List<CompletionResponse> loadCompletions(Long dailyRecordId) {
        return taskCompletionRepository.findByDailyRecordIdOrderByCompletedAtAsc(dailyRecordId)
                .stream()
                .map(CompletionResponse::from)
                .toList();
    }

    private TodayResponse emptyTodayResponse(LocalDate today) {
        return new TodayResponse(today, null, false, null, false, false, List.of());
    }

    private static OffsetDateTime toSeoul(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, SEOUL);
    }
}

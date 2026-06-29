package com.haru.backend.record.service;

import com.haru.backend.global.exception.BusinessException;
import com.haru.backend.global.exception.ErrorCode;
import com.haru.backend.record.dto.*;
import com.haru.backend.record.entity.CompletionType;
import com.haru.backend.record.entity.DailyRecord;
import com.haru.backend.record.entity.TaskCompletion;
import com.haru.backend.record.entity.UserStats;
import com.haru.backend.record.repository.DailyRecordRepository;
import com.haru.backend.record.repository.TaskCompletionRepository;
import com.haru.backend.record.repository.UserStatsRepository;
import com.haru.backend.task.entity.Task;
import com.haru.backend.task.entity.TaskType;
import com.haru.backend.task.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock DailyRecordRepository dailyRecordRepository;
    @Mock TaskCompletionRepository taskCompletionRepository;
    @Mock UserStatsRepository userStatsRepository;
    @Mock TaskRepository taskRepository;

    @InjectMocks
    RecordService recordService;

    private final UUID userId = UUID.randomUUID();

    // ── getToday ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getToday")
    class GetToday {

        @Test
        @DisplayName("오늘 daily_records가 없으면 currentTask null, 완료 목록 빈 응답")
        void noRecord() {
            given(dailyRecordRepository.findByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.empty());

            TodayResponse result = recordService.getToday(userId);

            assertThat(result.currentTask()).isNull();
            assertThat(result.fireEarned()).isFalse();
            assertThat(result.canFirstComplete()).isFalse();
            assertThat(result.canAdditionalComplete()).isFalse();
            assertThat(result.completedTasks()).isEmpty();
        }

        @Test
        @DisplayName("오늘의 한 개가 있고 첫 완료 전이면 canFirstComplete=true")
        void taskSetBeforeFirstComplete() {
            DailyRecord record = DailyRecord.create(userId, LocalDate.now());
            record.assignTask(1L, java.time.Instant.now());

            Task task = Task.create(userId, "운동하기", TaskType.GENERAL);

            given(dailyRecordRepository.findByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.of(record));
            given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId)).willReturn(Optional.of(task));
            given(taskCompletionRepository.findByDailyRecordIdOrderByCompletedAtAsc(record.getId()))
                    .willReturn(List.of());

            TodayResponse result = recordService.getToday(userId);

            assertThat(result.currentTask()).isNotNull();
            assertThat(result.currentTask().content()).isEqualTo("운동하기");
            assertThat(result.canFirstComplete()).isTrue();
            assertThat(result.canAdditionalComplete()).isFalse();
        }

        @Test
        @DisplayName("첫 완료 후에는 canFirstComplete=false, canAdditionalComplete=true")
        void afterFirstComplete() {
            DailyRecord record = DailyRecord.create(userId, LocalDate.now());
            record.recordFirstCompletion(java.time.Instant.now());

            given(dailyRecordRepository.findByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.of(record));
            given(taskCompletionRepository.findByDailyRecordIdOrderByCompletedAtAsc(record.getId()))
                    .willReturn(List.of());

            TodayResponse result = recordService.getToday(userId);

            assertThat(result.canFirstComplete()).isFalse();
            assertThat(result.canAdditionalComplete()).isTrue();
            assertThat(result.fireEarned()).isTrue();
        }
    }

    // ── createTodayTask ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("createTodayTask")
    class CreateTodayTask {

        @Test
        @DisplayName("새 task를 저장하고 daily_record에 current_task_id를 설정한다")
        void create() {
            given(dailyRecordRepository.findByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.empty());
            given(dailyRecordRepository.save(any(DailyRecord.class))).willAnswer(inv -> inv.getArgument(0));
            given(taskRepository.save(any(Task.class))).willAnswer(inv -> inv.getArgument(0));

            CreateTodayTaskRequest request = new CreateTodayTaskRequest("운동하기", TaskType.GENERAL);
            TodayTaskSetResponse result = recordService.createTodayTask(userId, request);

            verify(taskRepository).save(any(Task.class));
            assertThat(result.currentTask().content()).isEqualTo("운동하기");
            assertThat(result.currentTaskSelectedAt()).isNotNull();
        }

        @Test
        @DisplayName("taskType이 null이면 GENERAL로 저장된다")
        void defaultsToGeneral() {
            given(dailyRecordRepository.findByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.empty());
            given(dailyRecordRepository.save(any(DailyRecord.class))).willAnswer(inv -> inv.getArgument(0));
            given(taskRepository.save(any(Task.class))).willAnswer(inv -> inv.getArgument(0));

            CreateTodayTaskRequest request = new CreateTodayTaskRequest("운동하기", null);
            TodayTaskSetResponse result = recordService.createTodayTask(userId, request);

            ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(captor.capture());
            assertThat(captor.getValue().getTaskType()).isEqualTo(TaskType.GENERAL);
        }

        @Test
        @DisplayName("기존 daily_record가 있으면 새로 생성하지 않고 current_task_id만 교체한다")
        void replacesExistingRecord() {
            DailyRecord existing = DailyRecord.create(userId, LocalDate.now());
            given(dailyRecordRepository.findByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.of(existing));
            // mock save does not trigger @GeneratedValue — set id manually to simulate persistence
            given(taskRepository.save(any(Task.class))).willAnswer(inv -> {
                Task t = inv.getArgument(0);
                ReflectionTestUtils.setField(t, "id", 1L);
                return t;
            });

            recordService.createTodayTask(userId, new CreateTodayTaskRequest("새 할 일", TaskType.GENERAL));

            verify(dailyRecordRepository, never()).save(any(DailyRecord.class));
            assertThat(existing.getCurrentTaskId()).isEqualTo(1L);
        }
    }

    // ── setTodayTask ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("setTodayTask")
    class SetTodayTask {

        @Test
        @DisplayName("정상적으로 기존 할 일을 오늘의 한 개로 설정한다")
        void set() {
            Task task = Task.create(userId, "운동하기", TaskType.GENERAL);
            given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId))
                    .willReturn(Optional.of(task));
            given(dailyRecordRepository.findByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.empty());
            given(dailyRecordRepository.save(any(DailyRecord.class))).willAnswer(inv -> inv.getArgument(0));

            TodayTaskSetResponse result = recordService.setTodayTask(userId, new SetTodayTaskRequest(1L));

            assertThat(result.currentTask().content()).isEqualTo("운동하기");
        }

        @Test
        @DisplayName("미존재·미소유·삭제된 task이면 TASK_NOT_FOUND를 던진다")
        void taskNotFound() {
            given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(99L, userId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> recordService.setTodayTask(userId, new SetTodayTaskRequest(99L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TASK_NOT_FOUND);
        }
    }

    // ── clearTodayTask ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("clearTodayTask")
    class ClearTodayTask {

        @Test
        @DisplayName("daily_record가 없으면 새로 생성하지 않는다")
        void noRecord() {
            given(dailyRecordRepository.findByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.empty());

            recordService.clearTodayTask(userId);

            verify(dailyRecordRepository, never()).save(any());
        }

        @Test
        @DisplayName("current_task_id와 current_task_selected_at을 null로 변경한다")
        void clears() {
            DailyRecord record = DailyRecord.create(userId, LocalDate.now());
            record.assignTask(1L, java.time.Instant.now());
            given(dailyRecordRepository.findByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.of(record));

            recordService.clearTodayTask(userId);

            assertThat(record.getCurrentTaskId()).isNull();
            assertThat(record.getCurrentTaskSelectedAt()).isNull();
        }

        @Test
        @DisplayName("첫 완료 후 해제해도 fireEarned와 firstCompletedAt은 유지된다")
        void fireEarnedKeptAfterClear() {
            DailyRecord record = DailyRecord.create(userId, LocalDate.now());
            record.recordFirstCompletion(java.time.Instant.now());
            given(dailyRecordRepository.findByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.of(record));

            recordService.clearTodayTask(userId);

            assertThat(record.isFireEarned()).isTrue();
            assertThat(record.getFirstCompletedAt()).isNotNull();
            assertThat(record.hasFirstCompletion()).isTrue();
        }
    }

    // ── completeFirst ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("completeFirst")
    class CompleteFirst {

        @Test
        @DisplayName("daily_record가 없으면 TODAY_TASK_NOT_SELECTED를 던진다")
        void noRecord() {
            given(dailyRecordRepository.findWithLockByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> recordService.completeFirst(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TODAY_TASK_NOT_SELECTED);
        }

        @Test
        @DisplayName("current_task_id가 없으면 TODAY_TASK_NOT_SELECTED를 던진다")
        void noTaskSelected() {
            DailyRecord record = DailyRecord.create(userId, LocalDate.now());
            given(dailyRecordRepository.findWithLockByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.of(record));

            assertThatThrownBy(() -> recordService.completeFirst(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TODAY_TASK_NOT_SELECTED);
        }

        @Test
        @DisplayName("이미 첫 완료가 존재하면 ALREADY_COMPLETED_TODAY를 던진다")
        void alreadyCompleted() {
            DailyRecord record = DailyRecord.create(userId, LocalDate.now());
            record.assignTask(1L, java.time.Instant.now());
            record.recordFirstCompletion(java.time.Instant.now());
            given(dailyRecordRepository.findWithLockByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.of(record));

            assertThatThrownBy(() -> recordService.completeFirst(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ALREADY_COMPLETED_TODAY);
        }

        @Test
        @DisplayName("task가 미존재·미소유·삭제되면 TASK_NOT_FOUND를 던진다")
        void taskNotFound() {
            DailyRecord record = DailyRecord.create(userId, LocalDate.now());
            record.assignTask(1L, java.time.Instant.now());
            given(dailyRecordRepository.findWithLockByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.of(record));
            given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> recordService.completeFirst(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TASK_NOT_FOUND);
        }

        @Test
        @DisplayName("같은 날 같은 task 완료 기록이 있으면 TASK_ALREADY_COMPLETED_TODAY를 던진다")
        void taskAlreadyCompletedToday() {
            DailyRecord record = DailyRecord.create(userId, LocalDate.now());
            record.assignTask(1L, java.time.Instant.now());
            Task task = Task.create(userId, "운동하기", TaskType.GENERAL);

            given(dailyRecordRepository.findWithLockByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.of(record));
            given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId))
                    .willReturn(Optional.of(task));
            given(taskCompletionRepository.existsByDailyRecordIdAndTaskId(record.getId(), task.getId()))
                    .willReturn(true);

            assertThatThrownBy(() -> recordService.completeFirst(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TASK_ALREADY_COMPLETED_TODAY);
        }

        @Test
        @DisplayName("정상 첫 완료 시 completion 저장, fireEarned=true, streak 반환")
        void success() {
            DailyRecord record = DailyRecord.create(userId, LocalDate.now());
            record.assignTask(1L, java.time.Instant.now());
            Task task = Task.create(userId, "운동하기", TaskType.GENERAL);
            TaskCompletion completion = TaskCompletion.create(record, task, CompletionType.FIRST, java.time.Instant.now());
            UserStats stats = UserStats.create(userId);

            given(dailyRecordRepository.findWithLockByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.of(record));
            given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId))
                    .willReturn(Optional.of(task));
            given(taskCompletionRepository.existsByDailyRecordIdAndTaskId(record.getId(), task.getId()))
                    .willReturn(false);
            given(taskCompletionRepository.save(any(TaskCompletion.class))).willReturn(completion);
            given(userStatsRepository.findByUserId(userId)).willReturn(Optional.of(stats));

            FirstCompleteResponse result = recordService.completeFirst(userId);

            assertThat(result.fireEarned()).isTrue();
            assertThat(record.hasFirstCompletion()).isTrue();
            assertThat(result.streak().currentStreak()).isEqualTo(1);
        }
    }

    // ── streak calculation ────────────────────────────────────────────────────

    @Nested
    @DisplayName("스트릭 계산")
    class StreakCalculation {

        private DailyRecord prepareRecord() {
            DailyRecord record = DailyRecord.create(userId, LocalDate.now());
            record.assignTask(1L, java.time.Instant.now());
            return record;
        }

        private void setupMocks(DailyRecord record, Task task, UserStats stats) {
            given(dailyRecordRepository.findWithLockByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.of(record));
            given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId))
                    .willReturn(Optional.of(task));
            given(taskCompletionRepository.existsByDailyRecordIdAndTaskId(any(), any()))
                    .willReturn(false);
            given(taskCompletionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(userStatsRepository.findByUserId(userId)).willReturn(Optional.of(stats));
        }

        @Test
        @DisplayName("첫 완료(last_success_date=null)이면 streak=1")
        void firstEver() {
            DailyRecord record = prepareRecord();
            Task task = Task.create(userId, "운동하기", TaskType.GENERAL);
            UserStats stats = UserStats.create(userId);
            setupMocks(record, task, stats);

            FirstCompleteResponse result = recordService.completeFirst(userId);

            assertThat(result.streak().currentStreak()).isEqualTo(1);
            assertThat(result.streak().totalSuccessDays()).isEqualTo(1);
        }

        @Test
        @DisplayName("어제 성공했으면 streak+1")
        void continuesFromYesterday() {
            DailyRecord record = prepareRecord();
            Task task = Task.create(userId, "운동하기", TaskType.GENERAL);
            UserStats stats = UserStats.create(userId);
            stats.applyFirstCompletion(LocalDate.now().minusDays(1));

            setupMocks(record, task, stats);

            FirstCompleteResponse result = recordService.completeFirst(userId);

            assertThat(result.streak().currentStreak()).isEqualTo(2);
        }

        @Test
        @DisplayName("공백이 있으면 streak=1로 리셋")
        void resetsAfterGap() {
            DailyRecord record = prepareRecord();
            Task task = Task.create(userId, "운동하기", TaskType.GENERAL);
            UserStats stats = UserStats.create(userId);
            stats.applyFirstCompletion(LocalDate.now().minusDays(3));

            setupMocks(record, task, stats);

            FirstCompleteResponse result = recordService.completeFirst(userId);

            assertThat(result.streak().currentStreak()).isEqualTo(1);
        }

        @Test
        @DisplayName("last_success_date가 오늘이면 streak을 다시 증가시키지 않는다")
        void sameDayIdempotent() {
            UserStats stats = UserStats.create(userId);
            stats.applyFirstCompletion(LocalDate.now()); // already today

            int streakBefore = stats.getCurrentStreak();
            int totalBefore = stats.getTotalSuccessDays();

            stats.applyFirstCompletion(LocalDate.now()); // called again same day

            assertThat(stats.getCurrentStreak()).isEqualTo(streakBefore);
            assertThat(stats.getTotalSuccessDays()).isEqualTo(totalBefore);
        }

        @Test
        @DisplayName("max_streak은 current_streak의 최댓값을 추적한다")
        void maxStreak() {
            UserStats stats = UserStats.create(userId);
            stats.applyFirstCompletion(LocalDate.now().minusDays(4));
            stats.applyFirstCompletion(LocalDate.now().minusDays(3));
            stats.applyFirstCompletion(LocalDate.now().minusDays(2));

            assertThat(stats.getMaxStreak()).isEqualTo(3);

            // gap: reset to 1
            stats.applyFirstCompletion(LocalDate.now());
            assertThat(stats.getCurrentStreak()).isEqualTo(1);
            assertThat(stats.getMaxStreak()).isEqualTo(3);
        }
    }

    // ── completeAdditional ────────────────────────────────────────────────────

    @Nested
    @DisplayName("completeAdditional")
    class CompleteAdditional {

        @Test
        @DisplayName("daily_record가 없으면 ADDITIONAL_COMPLETION_BEFORE_FIRST를 던진다")
        void noRecord() {
            given(dailyRecordRepository.findByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> recordService.completeAdditional(userId, new AdditionalCompleteRequest(2L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ADDITIONAL_COMPLETION_BEFORE_FIRST);
        }

        @Test
        @DisplayName("첫 완료 전에 추가 완료하면 ADDITIONAL_COMPLETION_BEFORE_FIRST를 던진다")
        void beforeFirst() {
            DailyRecord record = DailyRecord.create(userId, LocalDate.now());
            given(dailyRecordRepository.findByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.of(record));

            assertThatThrownBy(() -> recordService.completeAdditional(userId, new AdditionalCompleteRequest(2L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ADDITIONAL_COMPLETION_BEFORE_FIRST);
        }

        @Test
        @DisplayName("같은 날 같은 task 중복 완료하면 TASK_ALREADY_COMPLETED_TODAY를 던진다")
        void duplicate() {
            DailyRecord record = DailyRecord.create(userId, LocalDate.now());
            record.recordFirstCompletion(java.time.Instant.now());
            Task task = Task.create(userId, "영양제 먹기", TaskType.RECURRING);

            given(dailyRecordRepository.findByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.of(record));
            given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(2L, userId))
                    .willReturn(Optional.of(task));
            given(taskCompletionRepository.existsByDailyRecordIdAndTaskId(record.getId(), task.getId()))
                    .willReturn(true);

            assertThatThrownBy(() -> recordService.completeAdditional(userId, new AdditionalCompleteRequest(2L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TASK_ALREADY_COMPLETED_TODAY);
        }

        @Test
        @DisplayName("정상 추가 완료 처리")
        void success() {
            DailyRecord record = DailyRecord.create(userId, LocalDate.now());
            record.recordFirstCompletion(java.time.Instant.now());
            Task task = Task.create(userId, "영양제 먹기", TaskType.RECURRING);
            TaskCompletion completion = TaskCompletion.create(record, task, CompletionType.ADDITIONAL, java.time.Instant.now());

            given(dailyRecordRepository.findByUserIdAndRecordDate(eq(userId), any(LocalDate.class)))
                    .willReturn(Optional.of(record));
            given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(2L, userId))
                    .willReturn(Optional.of(task));
            given(taskCompletionRepository.existsByDailyRecordIdAndTaskId(record.getId(), task.getId()))
                    .willReturn(false);
            given(taskCompletionRepository.save(any(TaskCompletion.class))).willReturn(completion);

            AdditionalCompleteResponse result =
                    recordService.completeAdditional(userId, new AdditionalCompleteRequest(2L));

            assertThat(result.completion().completionType()).isEqualTo(CompletionType.ADDITIONAL);
            assertThat(result.completion().content()).isEqualTo("영양제 먹기");
        }
    }

    // ── getStreak ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getStreak")
    class GetStreak {

        @Test
        @DisplayName("user_stats가 없으면 0으로 응답한다")
        void noStats() {
            given(userStatsRepository.findByUserId(userId)).willReturn(Optional.empty());

            StreakResponse result = recordService.getStreak(userId);

            assertThat(result.currentStreak()).isZero();
            assertThat(result.maxStreak()).isZero();
            assertThat(result.totalSuccessDays()).isZero();
            assertThat(result.lastSuccessDate()).isNull();
        }

        @Test
        @DisplayName("user_stats가 있으면 그 값을 반환한다")
        void withStats() {
            UserStats stats = UserStats.create(userId);
            stats.applyFirstCompletion(LocalDate.now().minusDays(1));
            stats.applyFirstCompletion(LocalDate.now());

            given(userStatsRepository.findByUserId(userId)).willReturn(Optional.of(stats));

            StreakResponse result = recordService.getStreak(userId);

            assertThat(result.currentStreak()).isEqualTo(2);
            assertThat(result.totalSuccessDays()).isEqualTo(2);
        }
    }
}

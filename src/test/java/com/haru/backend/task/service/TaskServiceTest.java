package com.haru.backend.task.service;

import com.haru.backend.global.exception.BusinessException;
import com.haru.backend.global.exception.ErrorCode;
import com.haru.backend.task.dto.TaskCreateRequest;
import com.haru.backend.task.dto.TaskOrderRequest;
import com.haru.backend.task.dto.TaskResponse;
import com.haru.backend.task.dto.TaskUpdateRequest;
import com.haru.backend.task.entity.Task;
import com.haru.backend.task.entity.TaskType;
import com.haru.backend.task.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private final UUID userId = UUID.randomUUID();

    private Task taskWithId(long id, String content, TaskType type) {
        Task task = Task.create(userId, content, type);
        ReflectionTestUtils.setField(task, "id", id);
        return task;
    }

    @Test
    @DisplayName("할 일을 생성하면 저장하고 응답을 반환한다")
    void create() {
        TaskCreateRequest request = new TaskCreateRequest("운동하기", TaskType.GENERAL);

        TaskResponse response = taskService.create(userId, request);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getContent()).isEqualTo("운동하기");
        assertThat(saved.getTaskType()).isEqualTo(TaskType.GENERAL);
        assertThat(response.content()).isEqualTo("운동하기");
    }

    @Test
    @DisplayName("taskType 이 null 이면 GENERAL 로 생성된다")
    void createDefaultsToGeneral() {
        TaskCreateRequest request = new TaskCreateRequest("운동하기", null);

        TaskResponse response = taskService.create(userId, request);

        assertThat(response.taskType()).isEqualTo(TaskType.GENERAL);
    }

    @Test
    @DisplayName("완료된 GENERAL 이 없으면 목록을 그대로 반환한다")
    void getTasksWithoutCompletion() {
        given(taskRepository.findAllByUserIdAndDeletedAtIsNullOrderByDisplayOrderAsc(userId))
                .willReturn(List.of(
                        taskWithId(1L, "운동하기", TaskType.GENERAL),
                        taskWithId(2L, "영양제 먹기", TaskType.RECURRING)
                ));
        given(taskRepository.findCompletedTaskIds(List.of(1L))).willReturn(List.of());

        List<TaskResponse> result = taskService.getTasks(userId);

        assertThat(result).extracting(TaskResponse::content)
                .containsExactly("운동하기", "영양제 먹기");
    }

    @Test
    @DisplayName("완료 기록이 있는 GENERAL 은 제외하고, RECURRING 은 완료돼도 포함한다")
    void getTasksExcludesCompletedGeneral() {
        given(taskRepository.findAllByUserIdAndDeletedAtIsNullOrderByDisplayOrderAsc(userId))
                .willReturn(List.of(
                        taskWithId(1L, "운동하기", TaskType.GENERAL),
                        taskWithId(2L, "영양제 먹기", TaskType.RECURRING),
                        taskWithId(3L, "물 마시기", TaskType.GENERAL)
                ));
        // GENERAL 후보(1L, 3L) 중 1L 만 완료됨
        given(taskRepository.findCompletedTaskIds(List.of(1L, 3L))).willReturn(List.of(1L));

        List<TaskResponse> result = taskService.getTasks(userId);

        // 완료된 GENERAL(운동하기) 제외, RECURRING(영양제) 포함, 미완료 GENERAL(물 마시기) 포함
        assertThat(result).extracting(TaskResponse::content)
                .containsExactly("영양제 먹기", "물 마시기");
    }

    @Test
    @DisplayName("내 할 일이면 내용을 수정한다")
    void updateContent() {
        Task task = Task.create(userId, "운동하기", TaskType.GENERAL);
        given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId))
                .willReturn(Optional.of(task));

        TaskResponse response =
                taskService.updateContent(userId, 1L, new TaskUpdateRequest("30분 운동하기"));

        assertThat(task.getContent()).isEqualTo("30분 운동하기");
        assertThat(response.content()).isEqualTo("30분 운동하기");
    }

    @Test
    @DisplayName("내 할 일이 아니거나 없으면 수정 시 TASK_NOT_FOUND 를 던진다")
    void updateContentNotFound() {
        given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateContent(userId, 1L, new TaskUpdateRequest("x")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.TASK_NOT_FOUND);
    }

    @Test
    @DisplayName("할 일을 삭제하면 deletedAt 이 채워진다(soft delete)")
    void delete() {
        Task task = Task.create(userId, "운동하기", TaskType.GENERAL);
        given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId))
                .willReturn(Optional.of(task));

        taskService.delete(userId, 1L);

        assertThat(task.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("반복 설정을 켜면 taskType 이 RECURRING 이 된다")
    void changeRecurring() {
        Task task = Task.create(userId, "영양제 먹기", TaskType.GENERAL);
        given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId))
                .willReturn(Optional.of(task));

        TaskResponse response = taskService.changeRecurring(userId, 1L, true);

        assertThat(task.getTaskType()).isEqualTo(TaskType.RECURRING);
        assertThat(response.taskType()).isEqualTo(TaskType.RECURRING);
    }

    @Test
    @DisplayName("순서 변경은 각 할 일의 displayOrder 를 갱신한다")
    void updateOrder() {
        Task t1 = Task.create(userId, "운동하기", TaskType.GENERAL);
        Task t2 = Task.create(userId, "영양제 먹기", TaskType.RECURRING);
        given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId)).willReturn(Optional.of(t1));
        given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(2L, userId)).willReturn(Optional.of(t2));

        taskService.updateOrder(userId, List.of(
                new TaskOrderRequest.OrderItem(1L, 5),
                new TaskOrderRequest.OrderItem(2L, 0)
        ));

        assertThat(t1.getDisplayOrder()).isEqualTo(5);
        assertThat(t2.getDisplayOrder()).isEqualTo(0);
    }

    @Test
    @DisplayName("순서 변경 중 내 할 일이 아닌 id 가 있으면 TASK_NOT_FOUND 를 던진다")
    void updateOrderNotFound() {
        given(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, userId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateOrder(userId, List.of(
                new TaskOrderRequest.OrderItem(1L, 0)
        )))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.TASK_NOT_FOUND);
    }
}

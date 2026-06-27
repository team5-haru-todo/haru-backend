package com.haru.backend.task.repository;

import com.haru.backend.global.config.JpaAuditingConfig;
import com.haru.backend.task.entity.Task;
import com.haru.backend.task.entity.TaskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager em;

    /**
     * tasks.user_id 는 users(id) 를 참조하는 FK 이므로, 테스트용 사용자 행을 먼저 만들고 UUID 를 확보한다.
     * DB 기본값(gen_random_uuid, NOW())을 활용해 native insert 로 처리한다.
     */
    private UUID insertUser() {
        Object id = em.getEntityManager()
                .createNativeQuery("INSERT INTO users (status) VALUES ('GUEST') RETURNING id")
                .getSingleResult();
        return UUID.fromString(id.toString());
    }

    @Test
    @DisplayName("삭제되지 않은 할 일만 display_order 오름차순으로 조회한다")
    void findActiveTasksOrderedByDisplayOrder() {
        UUID userId = insertUser();

        Task second = Task.create(userId, "운동하기", TaskType.GENERAL);
        second.changeDisplayOrder(1);
        Task first = Task.create(userId, "영양제 먹기", TaskType.RECURRING);
        first.changeDisplayOrder(0);
        Task deleted = Task.create(userId, "삭제된 할 일", TaskType.GENERAL);
        deleted.delete();

        taskRepository.saveAll(List.of(second, first, deleted));
        em.flush();
        em.clear();

        List<Task> result =
                taskRepository.findAllByUserIdAndDeletedAtIsNullOrderByDisplayOrderAsc(userId);

        assertThat(result)
                .extracting(Task::getContent)
                .containsExactly("영양제 먹기", "운동하기");
    }

    @Test
    @DisplayName("다른 사용자의 할 일은 단건 조회되지 않는다")
    void findByIdScopedToOwner() {
        UUID owner = insertUser();
        UUID other = insertUser();

        Task task = taskRepository.save(Task.create(owner, "내 할 일", TaskType.GENERAL));
        em.flush();

        assertThat(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(task.getId(), owner))
                .isPresent();
        assertThat(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(task.getId(), other))
                .isEmpty();
    }

    @Test
    @DisplayName("soft delete 된 할 일은 단건 조회되지 않는다")
    void findByIdExcludesDeleted() {
        UUID userId = insertUser();

        Task task = Task.create(userId, "삭제될 할 일", TaskType.GENERAL);
        task.delete();
        Task saved = taskRepository.save(task);
        em.flush();

        assertThat(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(saved.getId(), userId))
                .isEmpty();
    }
}

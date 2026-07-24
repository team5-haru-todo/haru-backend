package com.haru.backend.fcm.repository;

import com.haru.backend.fcm.entity.NotificationSentLog;
import com.haru.backend.global.config.JpaAuditingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class NotificationSentLogRepositoryTest {
    @Autowired
    private NotificationSentLogRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("저장된 (token, caseName, sentDate) 조합은 existsRK true를 반환")
    void existsByTokenAndCaseNameAndSentDate() {
        //given 로그 하나 저장
        LocalDate today = LocalDate.now();
        NotificationSentLog log = NotificationSentLog.builder()
                .token("token-1")
                .caseName("morning-no-task")
                .sentDate(today)
                .build();
        repository.save(log);
        em.flush(); // db에 실제 밀어넣기

        //when & then 있는 조합은 true 없으면 false
        assertThat(repository.existsByTokenAndCaseNameAndSentDate("token-1", "morning-no-task", today))
                .isTrue();
        assertThat(repository.existsByTokenAndCaseNameAndSentDate("token-1", "evening-no-task", today))
                .isFalse();



    }
    @Test
    @DisplayName("같은 (token, caseName, sentDate)를 두 번 저장하면 UNIQUE 제약 위반으로 예외 발생")
    void duplicateSaveViolatesUniqueConstraint() {
        //given 첫 번째 로그 저장
        LocalDate today = LocalDate.now();
        NotificationSentLog first = NotificationSentLog.builder()
                .token("token-1")
                .caseName("morning-no-task")
                .sentDate(today)
                .build();
        repository.save(first);
        em.flush();

        //when & then 같은 조합으로 또 저장하면 터져야 함
        NotificationSentLog duplicate = NotificationSentLog.builder()
                .token("token-1")
                .caseName("morning-no-task")
                .sentDate(today)
                .build();

        assertThatThrownBy(()->{
            repository.save(duplicate);
            em.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
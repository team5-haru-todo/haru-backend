package com.haru.backend.user.repository;

import com.haru.backend.global.config.JpaAuditingConfig;
import com.haru.backend.user.entity.User;
import com.haru.backend.user.entity.UserStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class UserStatsRepositoryTest {

    @Autowired
    private UserStatsRepository userStatsRepository;

    @Autowired
    private TestEntityManager em;

    private User persistUser() {
        // @UuidGenerator가 persist 시점에 UUID를 할당하므로 persist 직후 getId()를 사용 가능
        return em.persist(User.createGuest());
    }

    @Test
    @DisplayName("createDefault(user) 저장 후 findById(user.id)로 조회된다")
    void saveAndFindById() {
        User user = persistUser();
        userStatsRepository.save(UserStats.createDefault(user));
        em.flush();
        em.clear();

        Optional<UserStats> found = userStatsRepository.findById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("기본값: currentStreak=0, maxStreak=0, totalSuccessDays=0, lastSuccessDate=null")
    void defaultValues() {
        User user = persistUser();
        userStatsRepository.save(UserStats.createDefault(user));
        em.flush();
        em.clear();

        UserStats stats = userStatsRepository.findById(user.getId()).orElseThrow();

        assertThat(stats.getCurrentStreak()).isZero();
        assertThat(stats.getMaxStreak()).isZero();
        assertThat(stats.getTotalSuccessDays()).isZero();
        assertThat(stats.getLastSuccessDate()).isNull();
    }

    @Test
    @DisplayName("applyFirstCompletion 후 flush/clear하면 변경값이 DB에 반영된다")
    void applyFirstCompletionPersisted() {
        User user = persistUser();
        userStatsRepository.save(UserStats.createDefault(user));
        em.flush();
        em.clear();

        UserStats stats = userStatsRepository.findById(user.getId()).orElseThrow();
        LocalDate today = LocalDate.now();
        stats.applyFirstCompletion(today);
        em.flush();
        em.clear();

        UserStats updated = userStatsRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getCurrentStreak()).isEqualTo(1);
        assertThat(updated.getMaxStreak()).isEqualTo(1);
        assertThat(updated.getTotalSuccessDays()).isEqualTo(1);
        assertThat(updated.getLastSuccessDate()).isEqualTo(today);
    }
}

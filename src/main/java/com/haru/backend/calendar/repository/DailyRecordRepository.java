package com.haru.backend.calendar.repository;

import com.haru.backend.calendar.entity.DailyRecord;
import com.haru.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
//이번 달 유저의 기록들을 다 가져와라 달력에 뿌려야 함. 그럼 어떤

public interface DailyRecordRepository extends JpaRepository<DailyRecord ,Long> {
    List<DailyRecord> findByUserAndRecordDateBetween(User user, Date start, Date end);
}

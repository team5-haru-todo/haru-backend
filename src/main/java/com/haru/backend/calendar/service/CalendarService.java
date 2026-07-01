package com.haru.backend.calendar.service;

import com.haru.backend.calendar.dto.CalendarRecordResponse;
import com.haru.backend.calendar.repository.CalendarCompletionRepository;
import com.haru.backend.calendar.repository.CalendarRepository;
import com.haru.backend.record.entity.DailyRecord;
import com.haru.backend.record.entity.TaskCompletion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final CalendarRepository calendarRepository;
    private final CalendarCompletionRepository calendarCompletionRepository;

    public List<CalendarRecordResponse> getMonthlyRecords(UUID userId, int year, int month) {
        // year, month로 달력 한 페이지의 시작일과 마지막 일을 만든다.
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<DailyRecord> records = calendarRepository
                .findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(userId, start, end);

        if (records.isEmpty()) {
            return List.of();
        }

        List<Long> recordIds = records.stream()
                .map(DailyRecord::getId)
                .toList();

        Map<Long, List<TaskCompletion>> completionsByRecordId = calendarCompletionRepository
                .findByDailyRecordIds(recordIds)
                .stream()
                .collect(Collectors.groupingBy(TaskCompletion::getDailyRecordId));

        return records.stream()
                .map(record -> CalendarRecordResponse.from(
                        record,
                        completionsByRecordId.getOrDefault(record.getId(), List.of())
                ))
                .toList();
    }
}

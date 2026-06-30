package com.haru.backend.calendar.service;

import com.haru.backend.calendar.entity.DailyRecord;
import com.haru.backend.calendar.repository.DailyRecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional(readOnly=true)
@Service
public class DailyRecordService {
    private DailyRecordRepository dailyRecordRepository;
    public List<DailyRecord> getMontlyRecord(UUID userId, int year, int month) {
        
    }
}

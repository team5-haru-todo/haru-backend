package com.haru.backend.devicetoken.service;

/**
 * 예약 알림에 사용하는 제목/내용 한 쌍.
 */
public record ReminderMessage(String title, String body) {
}

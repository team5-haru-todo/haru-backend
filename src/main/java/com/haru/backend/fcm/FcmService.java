package com.haru.backend.fcm;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.haru.backend.fcm.entity.NotificationSentLog;
import com.haru.backend.fcm.repository.NotificationSentLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FcmService {
    private final NotificationSentLogRepository notificationSentLogRepository;

    public void sendMessage(
            String caseName,
            String token,
            String title,
            String body
    ){
        LocalDate today = LocalDate.now();

        //이미 발송했는지 DB 확인 로직
        boolean alreadySent = notificationSentLogRepository
                .existsByTokenAndCaseNameAndSentDate(token, caseName, today);
        //이미 발송했으면 중단
        if(alreadySent){
            System.out.println("이미 발송된 알림입니다. 스킵!");
            return;
        }
        //메세지 조립
        Message message = Message.builder()
                .setToken(token)                          //디바이스 토큰(누구한테)
                .setNotification(Notification.builder()   // 무슨 내용
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();
        try {
            //FirebaseConfig에서 초기화한 그 Firebase 가져오기 (초기화가 먼저 되는 것이 필수)
            //.send(message) FCM 서버로 실제 발송. 성공하면 메세지 반환
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("FCM 전송 성공 : " + response);
            notificationSentLogRepository.save(
                    NotificationSentLog.builder()
                            .token(token)
                            .caseName(caseName)
                            .sentDate(today)
                            .sentAt(LocalDateTime.now())
                            .build()
            );
        } catch (FirebaseMessagingException e) {
            System.out.println("FCM 전송 실패 " + e.getMessage());
        }
    }
}

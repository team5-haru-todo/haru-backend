package com.haru.backend.fcm;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmService {

    public void sendMessage(String token,String title, String body){
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
        } catch (FirebaseMessagingException e) {
            System.out.println("FCM 전송 실패 " + e.getMessage());
        }

    }
}

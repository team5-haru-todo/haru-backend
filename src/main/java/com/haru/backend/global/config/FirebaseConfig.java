package com.haru.backend.global.config;
import org.springframework.core.io.ClassPathResource;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

//서버 시작 시 관리자 권한을 가진 FirebaseApp 객체를 초기화하는 설정 클래스
@Configuration
@ConditionalOnProperty(
        name = "firebase.enabled",
        havingValue = "true",
        //CI에서 만약에 해당 파일이 없다고 하더라도 실행되도록 설정
        matchIfMissing = true
)
public class FirebaseConfig {

    @PostConstruct
    //앱 시작 직후 init 실행하여 초기화
    public void init() throws IOException {
        InputStream serviceAccount =
                new ClassPathResource("firebase-service-account.json").getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        if(FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
}

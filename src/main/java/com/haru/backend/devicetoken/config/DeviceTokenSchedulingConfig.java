package com.haru.backend.devicetoken.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class DeviceTokenSchedulingConfig {
    // 디바이스 토큰 도메인에서 쓰는 알림 스케줄러를 켜기 위한 설정이다.
}

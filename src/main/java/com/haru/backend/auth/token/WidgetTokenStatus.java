package com.haru.backend.auth.token;

public enum WidgetTokenStatus {
    ACTIVE ,// 유효
    ROTATED , //교체
    REVOKED, // 무효화(폐기) -> 아직 만료는 안 되었지만 재사용 탐지되어 폐기
}

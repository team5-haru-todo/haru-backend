package com.haru.backend.devicetoken.dto;

import com.haru.backend.devicetoken.Platform;
// 필드 생성, 둘 다 받는 생성자 생성, token,platform() 접근자 생성
public record DeviceTokenRequest(String token, Platform platform) {

}

package com.haru.backend.auth.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoAuthClient {

    private final RestClient restClient = RestClient.create("https://kapi.kakao.com");

    public KakaoUserInfoResponse getUserInfo(String accessToken) {
        return restClient.get()
                .uri("/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserInfoResponse.class);
    }
}
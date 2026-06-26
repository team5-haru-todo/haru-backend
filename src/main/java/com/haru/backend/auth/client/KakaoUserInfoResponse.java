package com.haru.backend.auth.client;

public record KakaoUserInfoResponse(
        Long id,
        KakaoAccount kakao_account
) {
    public record KakaoAccount(
            Profile profile
    ) {
        public record Profile(
                String nickname
        ) {}
    }

    public String nickname() {
        if (kakao_account == null || kakao_account.profile() == null) {
            return "카카오 사용자";
        }
        return kakao_account.profile().nickname();
    }
}
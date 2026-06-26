package com.haru.backend.auth.service;

import com.haru.backend.auth.dto.LoginResponse;
import com.haru.backend.global.security.JwtProvider;
import com.haru.backend.user.entity.User;
import com.haru.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public LoginResponse loginAsGuest() {
        User guest = userRepository.save(User.createGuest());
        String accessToken = jwtProvider.createAccessToken(guest.getId());
        return LoginResponse.of(accessToken, guest, Collections.emptyList());
    }
}
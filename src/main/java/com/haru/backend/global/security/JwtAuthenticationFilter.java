package com.haru.backend.global.security;

import com.haru.backend.global.security.repository.InvalidatedTokenRepository;
import com.haru.backend.user.entity.User;
import com.haru.backend.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            InvalidatedTokenRepository invalidatedTokenRepository,
            UserRepository userRepository
    ) {
        this.jwtProvider = jwtProvider;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String tokenId = jwtProvider.getTokenId(token);

                // 로그아웃/탈퇴 등으로 무효화된 토큰이면 인증을 심지 않음 (401 처리로 이어짐)
                if (tokenId != null && invalidatedTokenRepository.existsById(tokenId)) {
                    SecurityContextHolder.clearContext();
                } else {
                    UUID userId = jwtProvider.getUserId(token);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    updateLastActiveAtIfNeeded(userId);
                }
            } catch (Exception e) {
                // 유효하지 않은(위조/만료) 토큰: 인증을 심지 않고 통과시켜 인가 단계에서 401 처리한다.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    // 미사용 게스트 판단(40일 기준)에 쓰이는 마지막 활동일을 갱신한다.
    // 하루 1회만 갱신하도록 해서, 인증된 모든 요청마다 DB 쓰기가 발생하지 않게 한다.
    private void updateLastActiveAtIfNeeded(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.needsLastActiveAtUpdate()) {
                user.updateLastActiveAt();
                userRepository.save(user);
            }
        });
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}

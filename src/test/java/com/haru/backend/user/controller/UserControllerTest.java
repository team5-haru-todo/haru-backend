package com.haru.backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haru.backend.global.exception.BusinessException;
import com.haru.backend.global.exception.ErrorCode;
import com.haru.backend.global.security.JwtAuthenticationFilter;
import com.haru.backend.global.security.SecurityConfig;
import com.haru.backend.global.security.TokenInvalidationService;
import com.haru.backend.user.dto.UserSettingsRequest;
import com.haru.backend.user.dto.UserSettingsResponse;
import com.haru.backend.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthenticationFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TokenInvalidationService tokenInvalidationService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("설정 조회 응답에 두 튜토리얼 버전이 포함된다")
    void getMySettings() throws Exception {
        given(userService.getMySettings(userId)).willReturn(
                new UserSettingsResponse(true, "Asia/Seoul", true, 0, 0, false));

        mockMvc.perform(get("/api/users/me/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mainTutorialVersion").value(0))
                .andExpect(jsonPath("$.data.mainCompletedTutorialVersion").value(0));
    }

    @Test
    @DisplayName("mainTutorialVersion PATCH → 200 OK")
    void updateMainTutorialVersion() throws Exception {
        given(userService.updateMySettings(eq(userId), any(UserSettingsRequest.class)))
                .willReturn(new UserSettingsResponse(true, "Asia/Seoul", true, 1, 0, false));

        mockMvc.perform(patch("/api/users/me/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserSettingsRequest(null, null, null, 1, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mainTutorialVersion").value(1));
    }

    @Test
    @DisplayName("mainTutorialVersion 에 음수를 보내면 → 400 Bad Request")
    void updateWithNegativeVersionIsRejected() throws Exception {
        mockMvc.perform(patch("/api/users/me/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserSettingsRequest(null, null, null, -1, null, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_001"))
                .andExpect(jsonPath("$.errors[0].field").value("mainTutorialVersion"));
    }

    @Test
    @DisplayName("저장된 값보다 낮은 버전으로 PATCH 하면 → 400 Bad Request (USER_003)")
    void updateWithDowngradeIsRejected() throws Exception {
        given(userService.updateMySettings(eq(userId), any(UserSettingsRequest.class)))
                .willThrow(new BusinessException(ErrorCode.TUTORIAL_VERSION_DOWNGRADE_NOT_ALLOWED));

        mockMvc.perform(patch("/api/users/me/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserSettingsRequest(null, null, null, 0, null, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("USER_003"));
    }

    @Test
    @DisplayName("두 튜토리얼 버전을 한 요청으로 함께 PATCH 할 수 있다")
    void updateBothTutorialVersions() throws Exception {
        given(userService.updateMySettings(eq(userId), any(UserSettingsRequest.class)))
                .willReturn(new UserSettingsResponse(true, "Asia/Seoul", true, 1, 1, false));

        mockMvc.perform(patch("/api/users/me/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserSettingsRequest(null, null, null, 1, 1, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mainTutorialVersion").value(1))
                .andExpect(jsonPath("$.data.mainCompletedTutorialVersion").value(1));
    }
}

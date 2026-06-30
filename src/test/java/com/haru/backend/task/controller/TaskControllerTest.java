package com.haru.backend.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haru.backend.global.exception.BusinessException;
import com.haru.backend.global.exception.ErrorCode;
import com.haru.backend.global.security.JwtAuthenticationFilter;
import com.haru.backend.global.security.SecurityConfig;
import com.haru.backend.task.dto.TaskCreateRequest;
import com.haru.backend.task.dto.TaskResponse;
import com.haru.backend.task.dto.TaskUpdateRequest;
import com.haru.backend.task.entity.TaskType;
import com.haru.backend.task.service.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 컨트롤러 슬라이스 테스트. 보안 인프라(SecurityConfig, JwtAuthenticationFilter)는 제외하고
 * 필터를 끈 뒤 @LoginUser 용 인증만 수동 주입하여, 컨트롤러 동작(상태코드/검증/응답)만 검증한다.
 */
@WebMvcTest(controllers = TaskController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthenticationFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

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
    @DisplayName("할 일 생성 → 201 Created")
    void create() throws Exception {
        given(taskService.create(eq(userId), any(TaskCreateRequest.class)))
                .willReturn(new TaskResponse(1L, "운동하기", TaskType.GENERAL, 0,
                        Instant.parse("2026-06-30T00:00:00Z")));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskCreateRequest("운동하기", TaskType.GENERAL))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("할 일이 생성되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.content").value("운동하기"))
                .andExpect(jsonPath("$.data.taskType").value("GENERAL"));
    }

    @Test
    @DisplayName("content 가 비어 있으면 → 400 Bad Request")
    void createWithBlankContent() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskCreateRequest("", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_001"))
                .andExpect(jsonPath("$.errors[0].field").value("content"));
    }

    @Test
    @DisplayName("할 일 목록 조회 → 200 OK")
    void getTasks() throws Exception {
        given(taskService.getTasks(userId)).willReturn(List.of(
                new TaskResponse(1L, "운동하기", TaskType.GENERAL, 0, Instant.parse("2026-06-30T00:00:00Z")),
                new TaskResponse(2L, "영양제 먹기", TaskType.RECURRING, 1, Instant.parse("2026-06-30T00:00:00Z"))
        ));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].content").value("운동하기"))
                .andExpect(jsonPath("$.data[1].taskType").value("RECURRING"));
    }

    @Test
    @DisplayName("할 일 삭제 → 204 No Content (본문 없음)")
    void deleteTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("없는 할 일 수정 → 404 Not Found")
    void updateNotFound() throws Exception {
        given(taskService.updateContent(eq(userId), eq(99L), any(TaskUpdateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.TASK_NOT_FOUND));

        mockMvc.perform(patch("/api/tasks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskUpdateRequest("새 내용"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TASK_001"));
    }
}

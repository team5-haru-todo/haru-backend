package com.haru.backend.global.security;

import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 로그인한 사용자의 식별자(UUID)를 컨트롤러 파라미터로 주입받기 위한 애너테이션.
 *
 * <pre>
 * public ApiResponse&lt;...&gt; create(@LoginUser UUID userId, ...) { ... }
 * </pre>
 */
@Parameter(hidden = true)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {
}

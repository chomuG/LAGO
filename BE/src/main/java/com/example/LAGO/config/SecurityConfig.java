package com.example.LAGO.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 * 라고할때 프로젝트 보안 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (최신 문법)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/**").permitAll()  // API 경로 모두 허용
                        .requestMatchers("/swagger-ui/**").permitAll()  // Swagger UI 허용
                        .requestMatchers("/swagger-ui.html").permitAll()  // Swagger UI HTML 허용
                        .requestMatchers("/v3/api-docs/**").permitAll()  // OpenAPI 스펙 허용
                        .requestMatchers("/swagger-resources/**").permitAll()  // Swagger 리소스 허용
                        .requestMatchers("/webjars/**").permitAll()  // Swagger webjars 허용
                        .requestMatchers("/actuator/**").permitAll()  // Actuator 허용
                        .requestMatchers("/error").permitAll()  // 에러 페이지 허용
                        .anyRequest().permitAll()  // 임시로 모든 요청 허용
                )
                .formLogin(form -> form.disable())  // 폼 로그인 비활성화 (최신 문법)
                .httpBasic(basic -> basic.disable()); // HTTP Basic 인증 비활성화
                
        return http.build();
    }
}

package com.example.LAGO.config;

// 개발 단계에서 Spring Security 임시 비활성화
// 운영 환경에서는 다시 활성화 필요

/*
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 * 라고할때 프로젝트 보안 설정 - 개발 단계에서는 모든 접근 허용
 */
/*
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 완전 비활성화
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()  // 모든 요청 완전 허용
                )
                .formLogin(form -> form.disable())  // 폼 로그인 완전 비활성화
                .httpBasic(basic -> basic.disable())  // HTTP Basic 인증 완전 비활성화
                .logout(logout -> logout.disable())  // 로그아웃 기능 비활성화
                .sessionManagement(session -> session.disable());  // 세션 관리 비활성화
                
        return http.build();
    }
}
*/

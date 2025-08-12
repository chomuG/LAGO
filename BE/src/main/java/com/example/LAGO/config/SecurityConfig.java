package com.example.LAGO.config;

// 개발 단계에서 Spring Security 완전 비활성화
// SecurityConfig 클래스 자체를 주석 처리하여 Spring Security 비활성화
// 운영 환경에서는 다시 활성화 필요

/**
 * Spring Security 설정 - 현재 비활성화됨
 * 개발 완료 후 다시 활성화할 예정
 */

// 전체 클래스 주석 처리로 Spring Security 비활성화
/*
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session.disable());
                
        return http.build();
    }
}
*/

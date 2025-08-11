package com.example.LAGO.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;   // ← models 쪽만 사용
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.version:1.0.0}") private String appVersion;
    @Value("${server.port:8080}")  private String serverPort;
    @Value("${spring.profiles.active:dev}") private String activeProfile;

    @Bean
    @Primary
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())               // 서버 주소 설정
                .components(createComponents());
                //.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    private Info createApiInfo() {
        return new Info()
                .title("라고할때 (LAGO) API Documentation")
                .description("...생략(기존 텍스트 유지)...")
                .version(appVersion)
                .contact(new Contact().name("D203팀 백엔드 개발자")
                        .email("ssafy.d203@example.com")
                        .url("https://github.com/ssafy-d203"))
                .license(new License().name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    // ✅ 추천: 상대경로로 고정 — 접속한 도메인/포트를 그대로 사용
    private List<Server> createServers() {
        return List.of(new Server().url("/"));
    }

    private Components createComponents() {
        return new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Bearer Token Authentication"));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01-auth").displayName("인증/회원관리")
                .pathsToMatch("/api/auth/**").build();
    }

    @Bean public GroupedOpenApi stockApi()   { return GroupedOpenApi.builder().group("02-stocks").displayName("주식/거래").pathsToMatch("/api/stocks/**").build(); }
    @Bean public GroupedOpenApi accountApi() { return GroupedOpenApi.builder().group("03-accounts").displayName("계좌관리").pathsToMatch("/api/accounts/**").build(); }
    @Bean public GroupedOpenApi aiBotApi()   { return GroupedOpenApi.builder().group("04-ai-bots").displayName("AI 매매봇").pathsToMatch("/api/ai-bots/**").build(); }
    @Bean public GroupedOpenApi studyApi()   { return GroupedOpenApi.builder().group("05-study").displayName("차트학습/퀴즈").pathsToMatch("/api/study/**").build(); }
    @Bean public GroupedOpenApi newsApi()    { return GroupedOpenApi.builder().group("06-news").displayName("뉴스/공지").pathsToMatch("/api/news/**").build(); }
    @Bean public GroupedOpenApi userApi()    { return GroupedOpenApi.builder().group("07-users").displayName("마이페이지").pathsToMatch("/api/users/**").build(); }
    @Bean public GroupedOpenApi adminApi()   { return GroupedOpenApi.builder().group("08-admin").displayName("관리자/기타").pathsToMatch("/api/admin/**", "/api/frames/**", "/api/recaps/**").build(); }
}

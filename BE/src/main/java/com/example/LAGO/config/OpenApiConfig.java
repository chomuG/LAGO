package com.example.LAGO.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger OpenAPI 3.0 설정
 * 지침서 명세: 모든 API에 상세 Swagger 문서화 필수
 * 
 * JWT 인증, 환경별 서버 설정, API 그룹화 등 포함
 * Spring Boot 3.x + springdoc-openapi-starter-webmvc-ui 기반
 * 
 * @author D203팀 백엔드 개발자
 * @since 2025-08-06
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * 메인 OpenAPI 설정
     * 
     * @return OpenAPI 객체
     */
    @Bean
        @Primary
        public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .components(createComponents())
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * API 정보 설정
     * 
     * @return Info 객체
     */
    private Info createApiInfo() {
        return new Info()
                .title("라고할때 (LAGO) API Documentation")
                .description("""
                        # 라고할때 프로젝트 API 명세서
                        
                        ## 프로젝트 개요
                        - **팀명**: D203팀 (6인 팀)
                        - **구성**: Spring Boot 백엔드 + MySQL + Redis + Android 앱
                        - **주요 기능**: 모의투자, AI 매매봇, 실시간 주가, 차트학습, 퀴즈
                        
                        ## 기술 스택
                        - **Backend**: Spring Boot 3.5.4, Java 21 Virtual Threads
                        - **Database**: MySQL (EC2), Redis (캐싱)
                        - **Security**: JWT, Firebase Authentication
                        - **Documentation**: Swagger OpenAPI 3.0
                        - **Deployment**: Docker, Jenkins CI/CD, AWS EC2
                        
                        ## 주요 API 그룹
                        - **Auth**: 인증/회원가입 (`/api/auth/*`)
                        - **Stocks**: 주식 조회/거래 (`/api/stocks/*`)
                        - **Accounts**: 계좌 관리 (`/api/accounts/*`)
                        - **AI Bots**: AI 매매봇 (`/api/ai-bots/*`)
                        - **Study**: 차트학습/퀴즈 (`/api/study/*`)
                        - **News**: 뉴스/공지 (`/api/news/*`)
                        - **Users**: 마이페이지 (`/api/users/*`)
                        
                        ## 인증 방식
                        - JWT Bearer Token 사용
                        - Firebase UID 기반 사용자 식별
                        - Authorization: Bearer {token}
                        
                        ## 데이터베이스 연동
                        - EC2 MySQL 연동 (보안 강화: 환경변수 사용)
                        - JPA/Hibernate ORM
                        - 명세서 기준 테이블/컬럼 구조 엄격 준수
                        
                        ## 개발 지침
                        - 모든 API는 명세서와 정확히 일치
                        - 예외처리/Validation 필수
                        - 테스트코드 작성 권장
                        - Javadoc 상세 문서화
                        """)
                .version(appVersion)
                .contact(new Contact()
                        .name("D203팀 백엔드 개발자")
                        .email("ssafy.d203@example.com")
                        .url("https://github.com/ssafy-d203"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * 서버 설정 (환경별)
     * 
     * @return Server 리스트
     */
    private List<Server> createServers() {
        if ("prod".equals(activeProfile)) {
            return List.of(
                    new Server()
                            .url("https://k13d203.p.ssafy.io/api")
                            .description("Production Server (AWS EC2)"),
                    new Server()
                            .url("http://localhost:" + serverPort)
                            .description("Local Development Server")
            );
        } else if ("test".equals(activeProfile)) {
            return List.of(
                    new Server()
                            .url("https://test.k13d203.p.ssafy.io/api")
                            .description("Test Server (AWS EC2)"),
                    new Server()
                            .url("http://localhost:" + serverPort)
                            .description("Local Development Server")
            );
        } else {
            return List.of(
                    new Server()
                            .url("http://localhost:" + serverPort)
                            .description("Local Development Server"),
                    new Server()
                            .url("https://dev.k13d203.p.ssafy.io/api")
                            .description("Development Server (AWS EC2)")
            );
        }
    }

    /**
     * 보안 스키마 및 컴포넌트 설정
     * 
     * @return Components 객체
     */
    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer Token Authentication\n\n" +
                                        "사용법:\n" +
                                        "1. /api/auth/login으로 로그인\n" +
                                        "2. 응답의 accessToken을 복사\n" +
                                        "3. Authorize 버튼 클릭 후 'Bearer {token}' 형식으로 입력\n" +
                                        "4. 모든 인증이 필요한 API에 자동 적용됨"));
    }

    // 그룹 기능을 비활성화하여 모든 API를 한 번에 표시
    // GroupedOpenApi Bean들을 주석처리하면 기본적으로 모든 API가 한 화면에 표시됩니다
    
    /*
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01-auth")
                .displayName("인증/회원관리")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi stockApi() {
        return GroupedOpenApi.builder()
                .group("02-stocks")
                .displayName("주식/거래")
                .pathsToMatch("/api/stocks/**")
                .build();
    }

    @Bean
    public GroupedOpenApi accountApi() {
        return GroupedOpenApi.builder()
                .group("03-accounts")
                .displayName("계좌관리")
                .pathsToMatch("/api/accounts/**")
                .build();
    }

    @Bean
    public GroupedOpenApi aiBotApi() {
        return GroupedOpenApi.builder()
                .group("04-ai-bots")
                .displayName("AI 매매봇")
                .pathsToMatch("/api/ai-bots/**")
                .build();
    }

    @Bean
    public GroupedOpenApi studyApi() {
        return GroupedOpenApi.builder()
                .group("05-study")
                .displayName("차트학습/퀴즈")
                .pathsToMatch("/api/study/**")
                .build();
    }

    @Bean
    public GroupedOpenApi newsApi() {
        return GroupedOpenApi.builder()
                .group("06-news")
                .displayName("뉴스/공지")
                .pathsToMatch("/api/news/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("07-users")
                .displayName("마이페이지")
                .pathsToMatch("/api/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("08-admin")
                .displayName("관리자/기타")
                .pathsToMatch("/api/admin/**", "/api/frames/**", "/api/recaps/**")
                .build();
    }
    */
}

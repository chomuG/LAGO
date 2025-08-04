package com.example.LAGO.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 설정
 * 라고할때 프로젝트 API 문서화
 * Mattermost 알림 테스트: 2025-08-04
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI lagoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("라고할때 API")
                        .description("라고할때 투자 시뮬레이션 앱 백엔드 API")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("D203팀")
                                .email("ssafy@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("로컬 서버"),
                        new Server().url("http://i13d203.p.ssafy.io:8081").description("EC2 개발 서버"),
                        new Server().url("https://api.lago.com").description("운영 서버")
                ));
    }
}

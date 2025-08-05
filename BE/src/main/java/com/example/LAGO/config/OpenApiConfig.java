package com.example.LAGO.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 설정 클래스
 * SpringDoc OpenAPI 3.0을 사용하여 API 문서를 자동 생성
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("라고할때 API")
                        .description("라고할때 프로젝트의 REST API 문서")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("D203팀")
                                .url("https://github.com/SSAFY-D203")
                                .email("d203@ssafy.com")))
                .servers(List.of(
                        new Server().url("http://localhost:9000").description("로컬 개발 서버"),
                        new Server().url("http://i13d203.p.ssafy.io:8081").description("EC2 배포 서버")
                ));
    }
}

package com.example.LAGO.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // LocalDateTime 지원

        // 직렬화시 타임스탬프 대신 ISO-8601 포맷으로 출력하도록 설정 (옵션)
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        log.info("ObjectMapper Bean 생성 완료 - JSON 파싱용 (JavaTimeModule 등록됨)");

        return mapper;
    }
}

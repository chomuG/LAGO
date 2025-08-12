package com.example.LAGO.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * AI 관련 설정 클래스
 * 
 * 지침서 명세:
 * - FinBERT Flask 서버와의 HTTP 통신을 위한 RestTemplate 설정
 * - Jackson ObjectMapper 설정
 * - 타임아웃 및 연결 설정 최적화
 * 
 * @author D203팀 백엔드 개발자
 * @since 2025-08-06
 */
@Configuration
@Slf4j
public class AiConfiguration {

    /**
     * FinBERT 서버 통신용 RestTemplate Bean
     * <p>
     * 설정:
     * - 연결 타임아웃: 5초
     * - 읽기 타임아웃: 10초
     * - Java 21 Virtual Thread와 호환
     *
     * @return RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 타임아웃 설정
        factory.setConnectTimeout(5000);  // 연결 타임아웃: 5초
        factory.setReadTimeout(10000);    // 읽기 타임아웃: 10초

        RestTemplate restTemplate = new RestTemplate(factory);

        log.info("RestTemplate Bean 생성 완료 - FinBERT 서버 통신용");
        return restTemplate;
    }

}

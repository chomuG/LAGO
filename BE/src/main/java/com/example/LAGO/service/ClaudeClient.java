package com.example.LAGO.service;

import com.example.LAGO.dto.request.ClaudeRequest;
import com.example.LAGO.dto.response.ClaudeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${claude.api.key:S13P11D203-c5ba35f0-48f1-4e2d-a711-8d000235564e}")
    private String apiKey;
    
    private static final String CLAUDE_API_URL = "https://gms.ssafy.io/gmsapi/api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-3-7-sonnet-latest";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    
    public String summarizeNews(String newsTitle, String newsContent) {
        try {
            String prompt = createSummaryPrompt(newsTitle, newsContent);
            
            ClaudeRequest request = ClaudeRequest.builder()
                    .model(MODEL)
                    .maxTokens(1024)
                    .messages(List.of(
                            ClaudeRequest.Message.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    .build();
            
            HttpHeaders headers = createHeaders();
            HttpEntity<ClaudeRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("Claude API 호출 - 뉴스 요약 요청");
            
            ResponseEntity<ClaudeResponse> response = restTemplate.exchange(
                    CLAUDE_API_URL, HttpMethod.POST, entity, ClaudeResponse.class);
            
            ClaudeResponse result = response.getBody();
            if (result != null && result.getContent() != null && !result.getContent().isEmpty()) {
                String summary = result.getContent().get(0).getText();
                log.info("Claude API 응답 성공 - 요약 생성 완료 ({}토큰)", 
                        result.getUsage() != null ? result.getUsage().getOutputTokens() : "unknown");
                return summary;
            }
            
            log.warn("Claude API 응답이 비어있음");
            return "요약 생성 실패";
            
        } catch (Exception e) {
            log.error("Claude API 호출 실패: {}", e.getMessage());
            return "요약 생성 중 오류 발생";
        }
    }
    
    private String createSummaryPrompt(String title, String content) {
        return String.format("""
            다음 뉴스를 투자자 관점에서 3문장으로 요약해주세요.
            투자에 중요한 정보(기업실적, 주가영향요인, 시장전망 등)를 중심으로 핵심 내용만 간결하게 정리해주세요.
            
            제목: %s
            
            내용: %s
            
            요약:
            """, title, content);
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", ANTHROPIC_VERSION);
        return headers;
    }
}
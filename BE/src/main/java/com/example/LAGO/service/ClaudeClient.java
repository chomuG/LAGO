package com.example.LAGO.service;

import com.example.LAGO.config.ClaudeRateLimiter;
import com.example.LAGO.dto.request.ClaudeRequest;
import com.example.LAGO.dto.response.ClaudeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeClient {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    
    @Autowired
    private ClaudeRateLimiter rateLimiter;

    @Value("${claude.api.key:S13P11D203-c5ba35f0-48f1-4e2d-a711-8d000235564e}")
    private String apiKey;

    private static final String CLAUDE_API_URL = "https://gms.ssafy.io/gmsapi/api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-3-7-sonnet-latest";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    public String summarizeNews(String newsTitle, String newsContent) {
        // 🔄 요청 전 Rate Limiting 적용
        rateLimiter.waitIfNeeded();
        
        try {
            log.info("=== Claude API 호출 시작 (Cloudflare 우회) ===");
            log.info("Title 길이: {}, Content 길이: {}", 
                newsTitle != null ? newsTitle.length() : 0,
                newsContent != null ? newsContent.length() : 0);

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
            
            // 🚀 실제 브라우저 헤더 완전 모방
            HttpHeaders headers = createBrowserHeaders();
            
            HttpEntity<ClaudeRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("Claude API 호출 - Cloudflare 우회 시도");
            
            ResponseEntity<ClaudeResponse> response = restTemplate.exchange(
                    CLAUDE_API_URL,
                    HttpMethod.POST,
                    entity,
                    ClaudeResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ClaudeResponse result = response.getBody();
                
                if (result.getContent() != null && !result.getContent().isEmpty()) {
                    String summary = result.getContent().get(0).getText();
                    log.info("Claude API 응답 성공 - 길이: {}자", summary.length());
                    return summary;
                }
            }
            
            return "요약 생성 실패";
            
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            
            // Cloudflare 차단 감지
            if (responseBody.contains("cloudflare") || responseBody.contains("<!DOCTYPE") || 
                responseBody.contains("<html>")) {
                log.error("🚨 Cloudflare 차단 감지 - HTML 응답");
                log.error("HTML 응답 일부: {}", responseBody.substring(0, Math.min(300, responseBody.length())));
                
                // 🔄 재시도 로직
                return retryWithDelay(newsTitle, newsContent);
            }
            
            log.error("Claude API 호출 실패: {}", e.getStatusCode());
            return "요약 생성 중 오류 발생";
            
        } catch (Exception e) {
            log.error("Claude API 호출 실패: {}", e.getMessage());
            return "요약 생성 중 시스템 오류 발생";
        }
    }

    private String createSummaryPrompt(String title, String content) {
        // 📝 안전한 길이 제한
        String safeTitle = title != null ? title : "제목 없음";
        String safeContent = content != null ? content : "내용 없음";

        // 전체 프롬프트가 너무 길지 않도록 제한
        if (safeContent.length() > 1200) {  // 여유있게 1200자로 제한
            safeContent = safeContent.substring(0, 1200) + "...";
            log.info("Content 길이 제한 적용: 1200자로 줄임");
        }

        String prompt = String.format(
                "다음 뉴스를 투자자 관점에서 한국어 3문장으로 요약해줘.\n" +
                        "오직 JSON만 출력하고 다른 텍스트는 금지.\n" +
                        "형식: {\"sentences\":[\"...\",\"...\",\"...\"]}\n" +
                        "제약:\n" +
                        "- 각 문장 40~60자, 마침표로 끝낼 것\n" +
                        "- 문장 내부에 큰따옴표(\") 사용 금지, 이모지·불릿 금지\n" +
                        "구성:\n" +
                        "1) 핵심 결정/발언\n" +
                        "2) 배경·이유·주가/시장 영향\n" +
                        "3) 후속 일정·논란/다음 단계\n\n" +
                        "제목: %s\n\n" +
                        "내용: %s",
                safeTitle,
                safeContent
        );

        log.debug("생성된 프롬프트 길이: {}", prompt.length());
        return prompt;
    }
    
    /**
     * 🚀 실제 브라우저 헤더 완전 모방
     */
    private HttpHeaders createBrowserHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        // 기본 API 헤더
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", ANTHROPIC_VERSION);
        
        // 🛡️ Cloudflare 우회용 브라우저 헤더
        headers.set("User-Agent", 
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", 
            "application/json, text/plain, */*");
        headers.set("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
        headers.set("Accept-Encoding", "gzip, deflate, br");
        headers.set("Connection", "keep-alive");
        headers.set("Origin", "https://console.anthropic.com");
        headers.set("Referer", "https://console.anthropic.com/");
        headers.set("Sec-Fetch-Dest", "empty");
        headers.set("Sec-Fetch-Mode", "cors");
        headers.set("Sec-Fetch-Site", "cross-site");
        headers.set("Sec-Ch-Ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"");
        headers.set("Sec-Ch-Ua-Mobile", "?0");
        headers.set("Sec-Ch-Ua-Platform", "\"Windows\"");
        headers.set("Cache-Control", "no-cache");
        headers.set("Pragma", "no-cache");
        
        return headers;
    }
    
    /**
     * 🔄 지연 후 재시도
     */
    private String retryWithDelay(String newsTitle, String newsContent) {
        try {
            log.info("Cloudflare 차단으로 인한 재시도 - 3초 대기");
            Thread.sleep(3000);  // 3초 대기
            
            // 재시도 시에는 더 간단한 헤더 사용
            String prompt = createSummaryPrompt(newsTitle, newsContent);
            
            ClaudeRequest request = ClaudeRequest.builder()
                    .model(MODEL)
                    .maxTokens(512)  // 토큰 수 줄임
                    .messages(List.of(
                            ClaudeRequest.Message.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", ANTHROPIC_VERSION);
            headers.set("User-Agent", "PostmanRuntime/7.32.3");  // Postman User-Agent
            
            HttpEntity<ClaudeRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<ClaudeResponse> response = restTemplate.exchange(
                    CLAUDE_API_URL,
                    HttpMethod.POST,
                    entity,
                    ClaudeResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ClaudeResponse result = response.getBody();
                if (result.getContent() != null && !result.getContent().isEmpty()) {
                    log.info("재시도 성공");
                    return result.getContent().get(0).getText();
                }
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("재시도도 실패: {}", e.getMessage());
        }
        
        return "Cloudflare 차단으로 인한 요약 생성 실패";
    }
}
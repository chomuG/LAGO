package com.example.LAGO.service;

import com.example.LAGO.dto.request.ClaudeRequest;
import com.example.LAGO.dto.response.ClaudeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ClaudeClientOkHttp {
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Value("${claude.api.key:S13P11D203-c5ba35f0-48f1-4e2d-a711-8d000235564e}")
    private String apiKey;
    
    private static final String CLAUDE_API_URL = "https://gms.ssafy.io/gmsapi/api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-3-7-sonnet-latest";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    
    public ClaudeClientOkHttp(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        
        // 🚀 OkHttp 클라이언트 - Cloudflare 우회 최적화
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(this::addBrowserHeaders)  // 자동 헤더 추가
            .addInterceptor(this::logRequestResponse)  // 요청/응답 로깅
            .retryOnConnectionFailure(true)
            .build();
    }
    
    private Response addBrowserHeaders(Interceptor.Chain chain) throws IOException {
        Request original = chain.request();
        
        Request.Builder requestBuilder = original.newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Accept", "application/json, text/plain, */*")
            .header("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Connection", "keep-alive")
            .header("Origin", "https://console.anthropic.com")
            .header("Referer", "https://console.anthropic.com/")
            .header("Sec-Fetch-Dest", "empty")
            .header("Sec-Fetch-Mode", "cors")
            .header("Sec-Fetch-Site", "cross-site")
            .header("Sec-Ch-Ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"")
            .header("Sec-Ch-Ua-Mobile", "?0")
            .header("Sec-Ch-Ua-Platform", "\"Windows\"")
            .header("Cache-Control", "no-cache")
            .header("Pragma", "no-cache");
        
        return chain.proceed(requestBuilder.build());
    }
    
    /**
     * 🔍 요청/응답 상세 로깅 인터셉터
     */
    private Response logRequestResponse(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        
        // 요청 로깅
        log.info("🚀 OkHttp Request: {} {}", request.method(), request.url());
        log.debug("Request Headers: {}", request.headers());
        
        if (request.body() != null && request.body().contentLength() < 2000) {
            try {
                okio.Buffer buffer = new okio.Buffer();
                request.body().writeTo(buffer);
                log.debug("Request Body: {}", buffer.readUtf8());
            } catch (Exception e) {
                log.debug("Request Body 읽기 실패: {}", e.getMessage());
            }
        }
        
        long startTime = System.currentTimeMillis();
        Response response = chain.proceed(request);
        long endTime = System.currentTimeMillis();
        
        // 응답 로깅
        log.info("📡 OkHttp Response: {} {} ({}ms)", 
            response.code(), response.message(), endTime - startTime);
        log.debug("Response Headers: {}", response.headers());
        
        // 응답 본문 로깅 (최대 1000자)
        if (response.body() != null) {
            ResponseBody responseBody = response.body();
            String responseString = responseBody.string();
            
            log.info("📄 Response Body ({}bytes): {}", 
                responseString.length(),
                responseString.length() > 1000 ? 
                    responseString.substring(0, 1000) + "..." : 
                    responseString);
                    
            // ResponseBody를 다시 생성해서 반환 (한 번 읽으면 소모되므로)
            response = response.newBuilder()
                .body(ResponseBody.create(responseString, responseBody.contentType()))
                .build();
        }
        
        return response;
    }
    
    public String summarizeNews(String newsTitle, String newsContent) {
        try {
            log.info("=== OkHttp Claude API 호출 시작 ===");
            log.info("Title: {}, Content 길이: {}", 
                newsTitle != null ? newsTitle.substring(0, Math.min(30, newsTitle.length())) : "null",
                newsContent != null ? newsContent.length() : 0);
            
            String prompt = createSummaryPrompt(newsTitle, newsContent);
            
            ClaudeRequest request = ClaudeRequest.builder()
                    .model(MODEL)
                    .maxTokens(500)  // 토큰 수를 줄여서 응답 시간 단축
                    .messages(List.of(
                            ClaudeRequest.Message.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    .build();
            
            String requestBody = objectMapper.writeValueAsString(request);
            log.debug("Request body 길이: {}", requestBody.length());
            
            // 🔥 OkHttp 요청
            Request httpRequest = new Request.Builder()
                    .url(CLAUDE_API_URL)
                    .post(RequestBody.create(requestBody, MediaType.get("application/json; charset=utf-8")))
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", ANTHROPIC_VERSION)
                    .addHeader("anthropic-dangerous-direct-browser-access", "true")  // SSAFY 게이트웨이 요구사항
                    .build();
            
            log.info("🚀 OkHttp로 Claude API 호출 중...");
            
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    log.error("응답 body가 null");
                    return "응답 없음";
                }
                
                String responseString = responseBody.string();
                
                log.info("=== OkHttp 응답 ===");
                log.info("응답 상태: {} {}", response.code(), response.message());
                log.info("응답 길이: {}", responseString.length());
                log.debug("응답 일부: {}", responseString.substring(0, Math.min(200, responseString.length())));
                
                if (response.isSuccessful()) {
                    ClaudeResponse result = objectMapper.readValue(responseString, ClaudeResponse.class);
                    
                    if (result.getContent() != null && !result.getContent().isEmpty()) {
                        String summary = result.getContent().get(0).getText();
                        log.info("✅ OkHttp Claude 요약 성공 - 길이: {}자", summary.length());
                        
                        // 토큰 사용량 로깅
                        if (result.getUsage() != null) {
                            log.info("토큰 사용량 - Input: {}, Output: {}", 
                                result.getUsage().getInputTokens(), 
                                result.getUsage().getOutputTokens());
                        }
                        
                        return summary;
                    } else {
                        log.warn("응답에 content가 없음");
                        return "요약 내용 없음";
                    }
                } else {
                    // Cloudflare 차단 감지
                    if (responseString.contains("<!DOCTYPE") || 
                        responseString.contains("<html>") || 
                        responseString.contains("cloudflare")) {
                        log.error("🚨 OkHttp도 Cloudflare 차단됨");
                        log.error("HTML 응답: {}", responseString.substring(0, Math.min(300, responseString.length())));
                        
                        // 재시도 로직
                        return retryWithSimplifiedHeaders(newsTitle, newsContent);
                    } else {
                        log.error("❌ Claude API 에러: {} - {}", response.code(), responseString);
                        return "API 호출 실패: " + response.code();
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("💥 OkHttp Claude API 호출 실패: {}", e.getMessage(), e);
            return "요약 생성 중 시스템 오류";
        }
    }
    
    /**
     * 🔄 간소화된 헤더로 재시도
     */
    private String retryWithSimplifiedHeaders(String newsTitle, String newsContent) {
        try {
            log.info("⏳ Cloudflare 우회를 위한 재시도 - 3초 대기");
            Thread.sleep(3000);
            
            String prompt = createSummaryPrompt(newsTitle, newsContent);
            
            ClaudeRequest request = ClaudeRequest.builder()
                    .model(MODEL)
                    .maxTokens(300)  // 더 적은 토큰으로 재시도
                    .messages(List.of(
                            ClaudeRequest.Message.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    .build();
            
            String requestBody = objectMapper.writeValueAsString(request);
            
            // 🔄 간소화된 헤더 사용
            Request httpRequest = new Request.Builder()
                    .url(CLAUDE_API_URL)
                    .post(RequestBody.create(requestBody, MediaType.get("application/json; charset=utf-8")))
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", ANTHROPIC_VERSION)
                    .addHeader("anthropic-dangerous-direct-browser-access", "true")  // SSAFY 게이트웨이 요구사항
                    .addHeader("User-Agent", "okhttp/4.12.0")  // OkHttp 기본 User-Agent
                    .build();
            
            // 새로운 간소화된 클라이언트
            OkHttpClient retryClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
            
            try (Response response = retryClient.newCall(httpRequest).execute()) {
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    return "재시도 실패: 응답 없음";
                }
                
                String responseString = responseBody.string();
                log.info("재시도 응답 상태: {}", response.code());
                
                if (response.isSuccessful()) {
                    ClaudeResponse result = objectMapper.readValue(responseString, ClaudeResponse.class);
                    
                    if (result.getContent() != null && !result.getContent().isEmpty()) {
                        String summary = result.getContent().get(0).getText();
                        log.info("✅ 재시도 성공!");
                        return summary;
                    }
                }
                
                log.error("재시도도 실패: {}", responseString.substring(0, Math.min(200, responseString.length())));
                return "Cloudflare 차단으로 요약 불가";
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "재시도 중단됨";
        } catch (Exception e) {
            log.error("재시도 중 오류: {}", e.getMessage());
            return "재시도 실패";
        }
    }
    
    private String createSummaryPrompt(String title, String content) {
        String safeTitle = title != null ? title : "제목 없음";
        String safeContent = content != null ? content : "내용 없음";
        
        // Cloudflare 차단을 줄이기 위해 프롬프트 길이 제한
        if (safeContent.length() > 800) {
            safeContent = safeContent.substring(0, 800) + "...";
            log.debug("Content 길이 800자로 제한");
        }
        
        return String.format(
            "다음 뉴스를 투자자 관점에서 한국어로 3문장 요약해 주세요. " +
            "기업 실적, 주가 요인, 시장 전망 등 핵심 정보에 집중해주세요.\n\n" +
            "제목: %s\n\n" +
            "내용: %s\n\n" +
            "요약:", 
            safeTitle, 
            safeContent
        );
    }
}
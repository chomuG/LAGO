package com.example.LAGO.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GptClient {
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Value("${claude.api.key:S13P11D203-c5ba35f0-48f1-4e2d-a711-8d000235564e}")
    private String apiKey;
    
    private static final String GPT_API_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4.1-mini";
    
    public GptClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(this::logRequestResponse)
            .retryOnConnectionFailure(true)
            .build();
    }
    
    private Response logRequestResponse(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        
        log.info("🚀 GPT Request: {} {}", request.method(), request.url());
        log.debug("Request Headers: {}", request.headers());
        
        long startTime = System.currentTimeMillis();
        Response response = chain.proceed(request);
        long endTime = System.currentTimeMillis();
        
        log.info("📡 GPT Response: {} {} ({}ms)", 
            response.code(), response.message(), endTime - startTime);
        
        if (response.body() != null) {
            ResponseBody responseBody = response.body();
            String responseString = responseBody.string();
            
            log.info("📄 Response Body ({}bytes): {}", 
                responseString.length(),
                responseString.length() > 500 ? 
                    responseString.substring(0, 500) + "..." : 
                    responseString);
                    
            response = response.newBuilder()
                .body(ResponseBody.create(responseString, responseBody.contentType()))
                .build();
        }
        
        return response;
    }
    
    public String summarizeNews(String newsTitle, String newsContent) {
        try {
            log.info("=== GPT API 호출 시작 ===");
            log.info("Title: {}, Content 길이: {}", 
                newsTitle != null ? newsTitle.substring(0, Math.min(30, newsTitle.length())) : "null",
                newsContent != null ? newsContent.length() : 0);
            
            String prompt = createSummaryPrompt(newsTitle, newsContent);
            
            GptRequest request = new GptRequest();
            request.setModel(MODEL);
            request.setMaxTokens(4096);
            request.setTemperature(0.3);
            
            GptMessage systemMessage = new GptMessage();
            systemMessage.setRole("system");
            systemMessage.setContent("Answer in Korean");
            
            GptMessage userMessage = new GptMessage();
            userMessage.setRole("user");
            userMessage.setContent(prompt);
            
            request.setMessages(List.of(systemMessage, userMessage));
            
            String requestBody = objectMapper.writeValueAsString(request);
            log.debug("Request body 길이: {}", requestBody.length());
            
            Request httpRequest = new Request.Builder()
                    .url(GPT_API_URL)
                    .post(RequestBody.create(requestBody, MediaType.get("application/json; charset=utf-8")))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            log.info("🚀 GPT API 호출 중...");
            
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    log.error("응답 body가 null");
                    return "응답 없음";
                }
                
                String responseString = responseBody.string();
                
                log.info("=== GPT 응답 ===");
                log.info("응답 상태: {} {}", response.code(), response.message());
                log.info("응답 길이: {}", responseString.length());
                
                if (response.isSuccessful()) {
                    GptResponse result = objectMapper.readValue(responseString, GptResponse.class);
                    
                    if (result.getChoices() != null && !result.getChoices().isEmpty()) {
                        String summary = result.getChoices().get(0).getMessage().getContent();
                        log.info("✅ GPT 요약 성공 - 길이: {}자", summary.length());
                        
                        // 토큰 사용량 로깅
                        if (result.getUsage() != null) {
                            log.info("토큰 사용량 - Input: {}, Output: {}, Total: {}", 
                                result.getUsage().getPromptTokens(), 
                                result.getUsage().getCompletionTokens(),
                                result.getUsage().getTotalTokens());
                        }
                        
                        return summary;
                    } else {
                        log.warn("응답에 choices가 없음");
                        return "요약 내용 없음";
                    }
                } else {
                    log.error("❌ GPT API 에러: {} - {}", response.code(), responseString);
                    return "API 호출 실패: " + response.code();
                }
            }
            
        } catch (Exception e) {
            log.error("💥 GPT API 호출 실패: {}", e.getMessage(), e);
            return "요약 생성 중 시스템 오류";
        }
    }
    
    private String createSummaryPrompt(String title, String content) {
        String safeTitle = title != null ? title : "제목 없음";
        String safeContent = content != null ? content : "내용 없음";
        
        if (safeContent.length() > 1000) {
            safeContent = safeContent.substring(0, 1000) + "...";
            log.debug("Content 길이 1000자로 제한");
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
    
    @Data
    public static class GptRequest {
        private String model;
        @JsonProperty("max_tokens")
        private Integer maxTokens;
        private Double temperature;
        private List<GptMessage> messages;
    }
    
    @Data
    public static class GptMessage {
        private String role;
        private String content;
    }
    
    @Data
    public static class GptResponse {
        private List<GptChoice> choices;
        private GptUsage usage;
    }
    
    @Data
    public static class GptChoice {
        private GptMessage message;
    }
    
    @Data
    public static class GptUsage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
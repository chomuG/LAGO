package com.example.LAGO.controller;

import com.example.LAGO.service.ClaudeClient;
import com.example.LAGO.service.ClaudeClientOkHttp;
import com.example.LAGO.service.GptClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/claude")
@RequiredArgsConstructor
public class ClaudeController {
    
    private final ClaudeClient claudeClient;
    private final ClaudeClientOkHttp claudeClientOkHttp;
    private final GptClient gptClient;
    
    @PostMapping("/summarize")
    public ResponseEntity<String> summarizeNews(@RequestBody Map<String, String> request) {
        try {
            String newsTitle = request.get("newsTitle");
            String newsContent = request.get("newsContent");
            
            if (newsTitle == null || newsTitle.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("newsTitle is required and cannot be empty");
            }
            
            // newsContent가 비어있는 경우 제목만으로 기본 요약 생성
            if (newsContent == null || newsContent.trim().isEmpty()) {
                newsContent = "뉴스 내용을 확인할 수 없습니다.";
                log.warn("뉴스 내용이 비어있어 기본 메시지로 대체: {}", newsTitle);
            }
            
            log.info("뉴스 요약 요청 - 제목: {}", newsTitle.length() > 30 ? newsTitle.substring(0, 30) + "..." : newsTitle);
            
            // 🚀 GPT 클라이언트 사용으로 전환
            String summary = gptClient.summarizeNews(newsTitle, newsContent);
            
            log.info("뉴스 요약 완료");
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("뉴스 요약 실패: {}", e.getMessage(), e);  // 스택트레이스 포함
            return ResponseEntity.internalServerError().body("요약 생성 중 오류 발생: " + e.getMessage());
        }
    }
}
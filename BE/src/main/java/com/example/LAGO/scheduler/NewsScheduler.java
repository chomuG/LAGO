package com.example.LAGO.scheduler;

import com.example.LAGO.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.news.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class NewsScheduler {
    
    private final NewsService newsService;
    
    @Scheduled(fixedRate = 1200000) // 20분마다 실행 (1200000ms = 20분)
    public void collectNews() {
        log.info("=== 뉴스 수집 스케줄러 시작 - {} ===", LocalDateTime.now());
        
        try {
            // 일반 투자 뉴스 수집
            newsService.collectAndSaveGeneralNews();
            
            // 잠시 대기 (API 호출 제한 고려)
            Thread.sleep(5000);
            
            // 관심종목 뉴스 수집
            newsService.collectAndSaveStockNews();
            
            log.info("=== 뉴스 수집 스케줄러 완료 - {} ===", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("뉴스 수집 스케줄러 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    // 매일 자정에 오래된 뉴스 정리 (선택사항)
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupOldNews() {
        log.info("=== 오래된 뉴스 정리 시작 - {} ===", LocalDateTime.now());
        
        try {
            // 30일 이상 된 뉴스 삭제 로직 (필요시 구현)
            // newsRepository.deleteByCreatedAtBefore(LocalDateTime.now().minusDays(30));
            
            log.info("=== 오래된 뉴스 정리 완료 - {} ===", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("오래된 뉴스 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
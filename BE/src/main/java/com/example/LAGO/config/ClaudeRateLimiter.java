package com.example.LAGO.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClaudeRateLimiter {
    
    private static final long MIN_INTERVAL_MS = 5000; // 5초 간격
    private long lastRequestTime = 0;
    
    public synchronized void waitIfNeeded() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTime;
        
        if (elapsed < MIN_INTERVAL_MS) {
            try {
                long waitTime = MIN_INTERVAL_MS - elapsed;
                log.info("Rate limiting: {}ms 대기", waitTime);
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Rate limiter 대기 중단됨");
            }
        }
        
        lastRequestTime = System.currentTimeMillis();
    }
}
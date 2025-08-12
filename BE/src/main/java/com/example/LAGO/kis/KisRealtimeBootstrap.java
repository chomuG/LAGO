package com.example.LAGO.kis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kis.autostart", havingValue = "true", matchIfMissing = false)
public class KisRealtimeBootstrap {

    private final KisWebSocketService kisWebSocketService;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        try {
            log.info("🔌 [KIS] Autostart: connecting WebSocket sessions...");
            kisWebSocketService.startAll();   // 실제 연결

            log.info("📥 [KIS] Loading all stocks from DB and subscribing...");
            List<String> failed = kisWebSocketService.addAllStocksFromDatabase();

            Map<String, Set<String>> dist = kisWebSocketService.getUserSubscriptions();
            int total = kisWebSocketService.getTotalActiveSubscriptions();

            log.info("✅ [KIS] Subscriptions total={}, distribution={}, failed={}", total, dist, failed);
        } catch (Exception e) {
            log.error("❌ [KIS] Autostart failed", e);
        }
    }
}

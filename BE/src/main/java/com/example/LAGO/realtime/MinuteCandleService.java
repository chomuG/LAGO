package com.example.LAGO.realtime;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.example.LAGO.domain.StockInfo;
import com.example.LAGO.repository.StockInfoRepository;
import lombok.Data;
import lombok.Builder;
import com.example.LAGO.realtime.TickData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.LAGO.domain.StockMinute;
import com.example.LAGO.dto.StockMinuteDto;

import com.example.LAGO.realtime.MinuteCandleWebsocketController;
import com.example.LAGO.repository.StockMinuteRepository;
import org.springframework.scheduling.annotation.Scheduled;
import lombok.extern.slf4j.Slf4j;

// 1분봉 집계 : 임시 저장 + 집계 역할

@Slf4j
@Service
public class MinuteCandleService {
    private final Map<String, List<TickData>> minuteBucket = new ConcurrentHashMap<>();
    // String key = "종목코드_yyyyMMdd_HHmm"

    @Autowired
    private StockInfoRepository stockInfoRepository;
    
    @Autowired
    private StockMinuteRepository stockMinuteRepository;
    
    @Autowired
    private MinuteCandleWebsocketController websocketController;
    
    // 마지막으로 처리한 분을 추적 (중복 처리 방지)
    private volatile LocalDateTime lastProcessedMinute = null;

    public void addTick(TickData tick) {
        String key = tick.getMinuteKey();
        minuteBucket.computeIfAbsent(key, k -> new ArrayList<>()).add(tick);
        
        // 새로운 분이 시작되면 이전 분의 집계를 완료 처리
        checkAndProcessCompletedMinutes(tick.getTruncatedMinute());
    }
    
    /**
     * 새로운 분 도착 시 이전 분의 1분봉 집계를 완료 처리
     */
    private void checkAndProcessCompletedMinutes(LocalDateTime currentMinute) {
        if (currentMinute == null) return;
        
        // 1분 전 시간 계산
        LocalDateTime previousMinute = currentMinute.minusMinutes(1);
        
        // 이전 분을 아직 처리하지 않았다면 처리
        if (lastProcessedMinute == null || previousMinute.isAfter(lastProcessedMinute)) {
            processCompletedMinutesUpTo(previousMinute);
            lastProcessedMinute = previousMinute;
        }
    }
    
    /**
     * 지정된 시간까지의 완성된 분봉들을 처리
     */
    private void processCompletedMinutesUpTo(LocalDateTime upToMinute) {
        // 현재 버킷에서 완성된 분봉들을 찾아서 처리
        minuteBucket.entrySet().removeIf(entry -> {
            String minuteKey = entry.getKey();
            List<TickData> tickList = entry.getValue();
            
            if (tickList.isEmpty()) return true;
            
            // 키에서 종목코드와 시간 분리
            String[] keyParts = minuteKey.split("_");
            if (keyParts.length < 3) return true;
            
            String code = keyParts[0];
            LocalDateTime minuteTime = tickList.get(0).getTruncatedMinute();
            
            // 완성된 분인지 확인 (현재 분보다 이전)
            if (minuteTime != null && !minuteTime.isAfter(upToMinute)) {
                processCompletedMinute(code, minuteKey, tickList);
                return true; // 처리 완료된 항목 제거
            }
            return false; // 아직 완성되지 않은 항목 유지
        });
    }

    // Domain(Entity)용
    public StockMinute buildCandle(String code, String minuteKey, List<TickData> tickList) {
        if (tickList == null || tickList.isEmpty()) return null;

        // 정렬(시간순) - 보통 실시간 입력이지만, 혹시 out-of-order 대비
        tickList.sort(Comparator.comparing(TickData::getParsedDateTime));

        // 첫 번째와 마지막 틱 데이터 추출
        TickData firstTick = tickList.get(0);
        TickData lastTick = tickList.get(tickList.size() - 1);

        // open: 첫 번째, close: 마지막
        int openPrice = firstTick.getOpenPrice() != null ? firstTick.getOpenPrice() : firstTick.getClosePrice();
        int closePrice = lastTick.getClosePrice();

        // high, low
        int highPrice = tickList.stream()
                .mapToInt(TickData::getHighPrice)
                .max()
                .orElse(closePrice);
        int lowPrice = tickList.stream()
                .mapToInt(TickData::getLowPrice)
                .min()
                .orElse(closePrice);

        // volume 합계
        int volume = tickList.stream()
                .mapToInt(t -> t.getVolume() != null ? t.getVolume() : 0)
                .sum();

        // 집계 시간(분)
        LocalDateTime candleDateTime = firstTick.getTruncatedMinute();

        // (1) code로 stock_info_id 조회
        StockInfo stockInfo = stockInfoRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("해당 종목코드가 없습니다: " + code));

        // StockMinute 엔티티 생성 (stockInfoId 필드에 저장)
        return StockMinute.builder()
                .stockInfo(stockInfo)   // 실제 필드명에 맞게!
                .date(candleDateTime)
                .openPrice(openPrice)
                .highPrice(highPrice)
                .lowPrice(lowPrice)
                .closePrice(closePrice)
                .volume(volume)
                .build();
    }

    // DTO 용
    public StockMinuteDto buildCandleDto(String code, String minuteKey, List<TickData> tickList) {
        StockMinute stockMinute = buildCandle(code, minuteKey, tickList);
        if (stockMinute == null) return null;
        return StockMinuteDto.fromEntity(stockMinute);
    }

    /**
     * 완성된 1분봉 처리 (DB 저장 + WebSocket 전송)
     */
    private void processCompletedMinute(String code, String minuteKey, List<TickData> tickList) {
        try {
            // 1분봉 Entity 생성
            StockMinute stockMinute = buildCandle(code, minuteKey, tickList);
            if (stockMinute == null) {
                log.warn("⚠️ 1분봉 생성 실패 - Code: {}, Key: {}", code, minuteKey);
                return;
            }
            
            // 1. DB에 저장
            StockMinute savedEntity = stockMinuteRepository.save(stockMinute);
            log.info("💾 1분봉 DB 저장 완료 - Code: {}, Time: {}", code, savedEntity.getDate());
            
            // 2. DTO 변환 후 WebSocket으로 전송
            StockMinuteDto dto = StockMinuteDto.fromEntity(savedEntity);
            websocketController.sendMinuteCandle(dto);
            log.info("📡 1분봉 WebSocket 전송 완료 - Code: {}, StockInfoId: {}", code, dto.getStockInfoId());
            
        } catch (Exception e) {
            log.error("❌ 1분봉 처리 실패 - Code: {}, Key: {}, Error: {}", code, minuteKey, e.getMessage(), e);
        }
    }
    
    /**
     * 스케줄러: 매 분마다 완성된 1분봉 처리 (혹시 놓친 것들 처리)
     */
    @Scheduled(cron = "30 * * * * *") // 매분 30초에 실행
    public void scheduleProcessCompletedMinutes() {
        LocalDateTime currentTime = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime oneMinuteAgo = currentTime.minusMinutes(1);
        
        log.debug("🕒 스케줄 실행 - 완성된 분봉 처리 ({}까지)", oneMinuteAgo);
        processCompletedMinutesUpTo(oneMinuteAgo);
    }
    
    /**
     * @deprecated 기존 메서드 (호환성 유지)
     */
    @Deprecated
    public void handleMinuteComplete(String code, String minuteKey) {
        List<TickData> tickList = minuteBucket.remove(minuteKey);
        if (tickList == null || tickList.isEmpty()) return;
        processCompletedMinute(code, minuteKey, tickList);
    }

}

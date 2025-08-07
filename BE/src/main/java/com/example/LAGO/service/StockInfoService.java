package com.example.LAGO.service;

import com.example.LAGO.domain.StockInfo;
import com.example.LAGO.domain.StockMinute;
import com.example.LAGO.dto.StockInfoDto;
import com.example.LAGO.repository.StockInfoRepository;
import com.example.LAGO.repository.StockMinuteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StockInfoService {

    private final StockInfoRepository stockInfoRepository;
    private final StockMinuteRepository stockMinuteRepository;

    public List<StockInfoDto> getAllStockInfo() {
        List<StockInfo> stockInfos = stockInfoRepository.findAll();
        List<StockInfoDto> result = new ArrayList<>();

        // 현재 시간 및 거래시간 계산
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        
        // 거래시간: 09:00:00 ~ 15:30:00
        LocalTime marketOpen = LocalTime.of(9, 0, 0);
        LocalTime marketClose = LocalTime.of(15, 30, 0);
        
        // 오늘 날짜의 거래시간 범위 설정
        LocalDateTime todayMarketStart = now.toLocalDate().atTime(marketOpen);
        LocalDateTime todayMarketEnd = now.toLocalDate().atTime(marketClose);
        
        // 15:30 이후면 15:30까지의 데이터, 그 전이면 현재 시간까지의 데이터
        LocalDateTime searchEndTime = currentTime.isAfter(marketClose) ? todayMarketEnd : now;

        for (StockInfo stock : stockInfos) {
            Double current = null;
            Double previous = null;
            Double rate = null;

            // 현재(또는 15:30 기준) 종가 조회
            List<StockMinute> currentMinutes = stockMinuteRepository
                    .findCurrentTradingPrice(stock.getStockInfoId(), todayMarketStart, searchEndTime);
            
            if (!currentMinutes.isEmpty()) {
                StockMinute currentMinute = currentMinutes.get(0); // 가장 최근 데이터
                current = (double) currentMinute.getClosePrice();
                
                // 이전 시간 종가 조회
                List<StockMinute> previousMinutes = stockMinuteRepository
                        .findPreviousTradingPrice(stock.getStockInfoId(), todayMarketStart, currentMinute.getDate());
                
                if (!previousMinutes.isEmpty()) {
                    StockMinute previousMinute = previousMinutes.get(0); // 가장 최근 이전 데이터
                    previous = (double) previousMinute.getClosePrice();
                    
                    // 등락률 계산: (현재가 - 이전가) / 이전가 * 100
                    if (previous != 0) {
                        double difference = current - previous;
                        rate = (difference / previous) * 100;
                        rate = Math.round(rate * 100.0) / 100.0; // 소수점 둘째자리 반올림
                    }
                }
            }

            StockInfoDto dto = new StockInfoDto(stock);
            dto.setCurrentClosePrice(current);
            dto.setPreviousClosePrice(previous);
            dto.setFluctuationRate(rate);

            result.add(dto);
        }

        return result;
    }
}

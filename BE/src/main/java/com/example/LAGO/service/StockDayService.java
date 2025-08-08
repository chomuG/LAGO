package com.example.LAGO.service;

import com.example.LAGO.domain.StockDay;
import com.example.LAGO.dto.StockDayDto;
import com.example.LAGO.repository.StockDayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class StockDayService {

    private final StockDayRepository stockDayRepository;

    @Autowired
    public StockDayService(StockDayRepository stockDayRepository) {
        this.stockDayRepository = stockDayRepository;
    }

    // 특정 종목의 특정 구간 일봉 데이터 조회 (DTO로 변환)
    // 캐싱 메모리(redis) 사용
    // redis 연결 문제로 일단 주석처리
//    @Cacheable(value = "stockDay", key = "#stockInfoId + ':' + #start + ':' + #end")
    public List<StockDayDto> getStockDays(Integer stockInfoId, LocalDate start, LocalDate end) {
        List<StockDay> entityList = stockDayRepository
                .findByStockInfoStockInfoIdAndDateBetweenOrderByDateAsc(stockInfoId, start, end);

        // Entity → DTO 변환
        return entityList.stream()
                .map(StockDayDto::fromEntity) // 정적 메서드 사용 (권장)
                .collect(Collectors.toList());
    }
}

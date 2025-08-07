package com.example.LAGO.service;

import com.example.LAGO.domain.StockMinute;
import com.example.LAGO.dto.StockMinuteDto;
import com.example.LAGO.repository.StockMinuteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockMinuteService {

    private final StockMinuteRepository stockMinuteRepository;

    @Autowired
    public StockMinuteService(StockMinuteRepository stockMinuteRepository) {
        this.stockMinuteRepository = stockMinuteRepository;
    }

    // 특정 종목의 특정 구간(시작~종료) 1분봉 데이터를 조회하여 DTO로 변환
    public List<StockMinuteDto> getMinutes(Integer stockInfoId, LocalDateTime start, LocalDateTime end) {
        List<StockMinute> entityList = stockMinuteRepository
                .findByStockInfoStockInfoIdAndDateBetweenOrderByDateAsc(stockInfoId, start, end);

        // Entity → DTO 변환
        return entityList.stream()
                .map(StockMinuteDto::fromEntity)
                .collect(Collectors.toList());
    }
}

package com.example.LAGO.service;

import com.example.LAGO.domain.StockInfo;
import com.example.LAGO.dto.StockInfoDto;
import com.example.LAGO.repository.StockInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StockInfoService {

    private final StockInfoRepository stockInfoRepository;

    public List<StockInfoDto> getAllStockInfo() {
        List<StockInfo> stockInfos = stockInfoRepository.findAll();
        return stockInfos.stream()
                .map(StockInfoDto::new)
                .collect(Collectors.toList());
    }
}

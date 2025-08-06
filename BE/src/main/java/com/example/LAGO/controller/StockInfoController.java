package com.example.LAGO.controller;

import com.example.LAGO.dto.StockInfoDto;
import com.example.LAGO.service.StockInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockInfoController {

    private final StockInfoService stockInfoService;

    @GetMapping
    public ResponseEntity<List<StockInfoDto>> getAllStockInfo() {
        List<StockInfoDto> stockInfos = stockInfoService.getAllStockInfo();
        return ResponseEntity.ok(stockInfos);
    }
}

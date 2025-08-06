package com.example.LAGO.controller;

import com.example.LAGO.dto.StockMinuteDto;
import com.example.LAGO.service.StockMinuteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/charts/stock-minute")
public class StockMinuteController {

    private final StockMinuteService stockMinuteService;

    @Autowired
    public StockMinuteController(StockMinuteService stockMinuteService) {
        this.stockMinuteService = stockMinuteService;
    }

    // GET: /api/charts/stock-minute?stockId=1&start=2024-07-01 12:00:00&end=2024-07-31 12:00:00
    @GetMapping
    public List<StockMinuteDto> getStockMinutes(
            @RequestParam("stockId") Integer stockInfoId,
            @RequestParam("start") LocalDateTime start,
            @RequestParam("end") LocalDateTime end
    ) {
        return stockMinuteService.getMinutes(stockInfoId, start, end);
    }
}

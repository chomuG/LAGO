package com.example.LAGO.controller;

import com.example.LAGO.dto.StockDayDto;
import com.example.LAGO.service.StockDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stock-day")
public class StockDayController {

    private final StockDayService stockDayService;

    @Autowired
    public StockDayController(StockDayService stockDayService) {
        this.stockDayService = stockDayService;
    }

    // GET: /api/stock-day?stockId=1&start=2024-07-01&end=2024-07-31
    @GetMapping
    public List<StockDayDto> getStockDays(
            @RequestParam("stockId") Integer stockInfoId,
            @RequestParam("start") LocalDate start,
            @RequestParam("end") LocalDate end
    ) {
        return stockDayService.getStockDays(stockInfoId, start, end);
    }
}

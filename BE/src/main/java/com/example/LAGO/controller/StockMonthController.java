package com.example.LAGO.controller;

import com.example.LAGO.dto.StockMonthDto;
import com.example.LAGO.service.StockMonthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stock-month")
public class StockMonthController {

    private final StockMonthService stockMonthService;

    @Autowired
    public StockMonthController(StockMonthService stockMonthService) {
        this.stockMonthService = stockMonthService;
    }

    // GET: /api/stock-month?stockId=1&start=202407&end=202412
    @GetMapping
    public List<StockMonthDto> getStockMonths(
            @RequestParam("stockId") Integer stockInfoId,
            @RequestParam("start") Integer start,
            @RequestParam("end") Integer end
    ) {
        return stockMonthService.getStockMonths(stockInfoId, start, end);
    }
}
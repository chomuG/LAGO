package com.example.LAGO.controller;

import com.example.LAGO.dto.StockMonthDto;
import com.example.LAGO.dto.StockYearDto;
import com.example.LAGO.service.StockMonthService;
import com.example.LAGO.service.StockYearService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/charts/stock-year")
public class StockYearController {

    private final StockYearService stockYearService;

    @Autowired
    public StockYearController(StockYearService stockYearService) {
        this.stockYearService = stockYearService;
    }

    // GET: /api/charts/stock-month?stockId=1&start=2023&end=2024
    @GetMapping
    public List<StockYearDto> getStockYears(
            @RequestParam("stockId") Integer stockInfoId,
            @RequestParam("start") Integer start,
            @RequestParam("end") Integer end
    ) {
        return stockYearService.getStockYears(stockInfoId, start, end);
    }
}

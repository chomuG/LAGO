package com.example.LAGO.dto;

import com.example.LAGO.domain.StockInfo;
import lombok.Getter;

@Getter
public class StockInfoDto {
    private final Integer stockInfoId;
    private final String code;
    private final String name;
    private final String market;

    public StockInfoDto(StockInfo stockInfo) {
        this.stockInfoId = stockInfo.getStockInfoId();
        this.code = stockInfo.getCode();
        this.name = stockInfo.getName();
        this.market = stockInfo.getMarket();
    }
}
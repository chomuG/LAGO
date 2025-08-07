package com.example.LAGO.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockInfoDto {
    private Integer stockInfoId;
    private String code;
    private String name;
    private String market;

    private Double currentClosePrice;
    private Double previousClosePrice;
    private Double fluctuationRate;

    public StockInfoDto() {}

    public StockInfoDto(com.example.LAGO.domain.StockInfo stockInfo) {
        this.stockInfoId = stockInfo.getStockInfoId();
        this.code = stockInfo.getCode();
        this.name = stockInfo.getName();
        this.market = stockInfo.getMarket();
    }
}

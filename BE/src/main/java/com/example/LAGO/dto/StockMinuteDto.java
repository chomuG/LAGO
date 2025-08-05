package com.example.LAGO.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;
import com.example.LAGO.entity.StockMinute;

@Getter
@Setter
@Builder
public class StockMinuteDto {
    private LocalDateTime date;
    private Integer openPrice;
    private Integer highPrice;
    private Integer lowPrice;
    private Integer closePrice;
    private Integer volume;
}

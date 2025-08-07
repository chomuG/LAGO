package com.example.LAGO.dto;

import com.example.LAGO.domain.StockMinute;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

    // Entity -> DTO 변환
    public static StockMinuteDto fromEntity(StockMinute entity) {
        return StockMinuteDto.builder()
                .date(entity.getDate())
                .openPrice(entity.getOpenPrice())
                .highPrice(entity.getHighPrice())
                .lowPrice(entity.getLowPrice())
                .closePrice(entity.getClosePrice())
                .volume(entity.getVolume())
                .build();
    }
}

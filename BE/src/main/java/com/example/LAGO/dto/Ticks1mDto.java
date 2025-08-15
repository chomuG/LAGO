package com.example.LAGO.dto;

import com.example.LAGO.domain.Ticks1m;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
@Builder
public class Ticks1mDto {
    private Integer stockInfoId;
    private LocalDateTime bucket; // KST로 변환된 시간
    private String code; // 종목코드
    private Integer openPrice;
    private Integer highPrice;
    private Integer lowPrice;
    private Integer closePrice;
    private Long volume;
    
    // Entity -> DTO 변환 (UTC -> KST 변환)
    public static Ticks1mDto fromEntity(Ticks1m ticks1m) {
        return Ticks1mDto.builder()
                .stockInfoId(ticks1m.getStockInfoId())
                .bucket(ticks1m.getBucket().atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime())
                .code(ticks1m.getStockInfo() != null ? ticks1m.getStockInfo().getCode() : null)
                .openPrice(ticks1m.getOpenPrice())
                .highPrice(ticks1m.getHighPrice())
                .lowPrice(ticks1m.getLowPrice())
                .closePrice(ticks1m.getClosePrice())
                .volume(ticks1m.getVolume())
                .build();
    }
}
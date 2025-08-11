package com.example.LAGO.dto.response;

import com.example.LAGO.service.HistoryChallengeServiceImpl;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@NoArgsConstructor
public class HistoryChallengeDataResponse {

    @Schema(description = "행 번호", example = "1")
    private Long rowId;

    @Schema(description = "일시", example = "2025-08-09 15:10:00")
    private LocalDateTime date;

    @Schema(description = "시가")
    private Integer openPrice;

    @Schema(description = "고가")
    private Integer highPrice;

    @Schema(description = "저가")
    private Integer lowPrice;

    @Schema(description = "종가")
    private Integer closePrice;

    @Schema(description = "거래량")
    private Long volume;

    @Schema(name = "fluctuation_price")
    private Integer fluctuationPrice;

    @Schema(name = "fluctuation_rate")
    private Float fluctuationRate;

    private static final Logger log = LoggerFactory.getLogger(HistoryChallengeServiceImpl.class);

    public HistoryChallengeDataResponse(Object[] aggregatedData) {
        this.rowId = ((Long) aggregatedData[0]);
        this.date = ((Timestamp) aggregatedData[1]).toLocalDateTime();
        this.openPrice = ((Integer) aggregatedData[2]);
        this.highPrice = ((Integer) aggregatedData[3]);
        this.lowPrice = ((Integer) aggregatedData[4]);
        this.closePrice = ((Integer) aggregatedData[5]);
        this.volume = ((Long) aggregatedData[6]);
        this.fluctuationPrice = ((Integer) aggregatedData[7]);
        this.fluctuationRate = this.getFluctuationPrice().floatValue() / this.getOpenPrice() * 100;
    }
}
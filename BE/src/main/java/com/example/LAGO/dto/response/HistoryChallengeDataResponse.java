package com.example.LAGO.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class HistoryChallengeDataResponse {

    @Schema(description = "챌린지 주가 데이터 ID", example = "1")
    private Integer challengeDataId;

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

    public HistoryChallengeDataResponse(Object[] aggregatedData) {
        this.date = ((Timestamp) aggregatedData[0]).toLocalDateTime();
        this.openPrice = ((Integer) aggregatedData[1]);
        this.highPrice = ((Integer) aggregatedData[2]);
        this.lowPrice = ((Integer) aggregatedData[3]);
        this.closePrice = ((Integer) aggregatedData[4]);
        this.volume = ((Long) aggregatedData[5]);
        this.fluctuationPrice = ((Integer) aggregatedData[6]);
        BigDecimal bd = (BigDecimal) aggregatedData[7];
        this.fluctuationRate = bd == null ? null : bd.floatValue();
    }
}
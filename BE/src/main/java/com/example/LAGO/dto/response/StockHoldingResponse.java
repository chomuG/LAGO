package com.example.LAGO.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 보유주식 조회 응답 DTO
 */
@Data
@Builder
@Schema(description = "보유주식 정보")
public class StockHoldingResponse {

    @Schema(description = "보유 ID", example = "1")
    private Long holdingId;

    @Schema(description = "종목 코드", example = "005930")
    private String stockCode;

    @Schema(description = "종목명", example = "삼성전자")
    private String stockName;

    @Schema(description = "보유 수량", example = "100")
    private Integer quantity;

    @Schema(description = "평균 매수가", example = "75000")
    private Integer averagePrice;

    @Schema(description = "총 매수금액", example = "7500000")
    private Integer totalCost;

    @Schema(description = "현재가", example = "80000")
    private Integer currentPrice;

    @Schema(description = "현재 평가금액", example = "8000000")
    private Integer currentValue;

    @Schema(description = "손익", example = "500000")
    private Integer profitLoss;

    @Schema(description = "수익률 (%)", example = "6.67")
    private Double profitLossRate;

    @Schema(description = "최초 매수일")
    private LocalDateTime firstPurchaseDate;

    @Schema(description = "최근 거래일")
    private LocalDateTime lastTradeDate;

    @Schema(description = "시장", example = "KOSPI")
    private String market;

    @Schema(description = "섹터", example = "전자")
    private String sector;
}
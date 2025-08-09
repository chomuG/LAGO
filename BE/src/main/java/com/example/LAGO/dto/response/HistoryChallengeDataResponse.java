package com.example.LAGO.dto.response;

import com.example.LAGO.domain.HistoryChallengeData;
import com.example.LAGO.domain.StockDay; // StockDay 사용
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class HistoryChallengeDataResponse {

    @Schema(description = "챌린지 주가 데이터 ID", example = "1")
    private Integer challengeDataId;

    @Schema(description = "챌린지 ID", example = "1")
    private Integer challengeId;

    @Schema(description = "일시", example = "2025-08-09 15:10:00")
    private LocalDateTime eventDate;

    @Schema(description = "시가")
    private Integer openPrice;

    @Schema(description = "고가")
    private Integer highPrice;

    @Schema(description = "저가")
    private Integer lowPrice;

    @Schema(description = "종가")
    private Integer closePrice;

    @Schema(name = "fluctuation_rate")
    private Float fluctuationRate;

    @Schema(description = "거래량")
    private Integer volume;

    @Schema(description = "간격", example = "10M")
    private String intervalType;

    @Schema(description = "과거 시작일시", example = "2020-06-01 00:00:00")
    private LocalDateTime startOriginDate;

    @Schema(description = "과거 종료일시", example = "2020-06-01 00:00:00")
    private LocalDateTime endOriginDate;


    // StockDay 엔티티를 받는 생성자
    public HistoryChallengeDataResponse(HistoryChallengeData entity) {
        this.challengeDataId = entity.getChallengeDataId();
        this.challengeId = entity.getChallengeId();
        this.eventDate = entity.getEventDate();
        this.openPrice = entity.getOpenPrice();
        this.highPrice = entity.getHighPrice();
        this.lowPrice = entity.getLowPrice();
        this.closePrice = entity.getClosePrice();
        this.fluctuationRate = entity.getFluctuationRate();
        this.volume = entity.getVolume();
        this.intervalType = entity.getIntervalType();
        this.startOriginDate = entity.getStartOriginDate();
        this.endOriginDate = entity.getEndOriginDate();
    }
}
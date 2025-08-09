package com.example.LAGO.dto.response;

import com.example.LAGO.domain.HistoryChallenge;
import com.example.LAGO.domain.HistoryChallengeData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class HistoryChallengeResponse {

    @Schema(description = "챌린지 ID", example = "1")
    private Integer challengeId;

    @Schema(description = "챌린지 테마", example = "코로나")
    private String theme;

    @Schema(description = "종목명", example = "셀트리온")
    private String stockName;

    @Schema(description = "종목코드", example = "068270")
    private String stockCode;

    @Schema(description = "시작일시", example = "2025-08-09 15:00:00")
    private LocalDateTime startDate;

    @Schema(description = "종료일시", example = "2025-08-20 21:00:00")
    private LocalDateTime endDate;

    @Schema(description = "현재 주가 정보")
    private HistoryChallengeDataResponse currentPriceData;

    public HistoryChallengeResponse(HistoryChallenge entity) {
        this.challengeId = entity.getChallengeId();
        this.theme = entity.getTheme();
        this.stockName = entity.getStockName();
        this.stockCode = entity.getStockCode();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.currentPriceData = null; // 기본 생성자에서는 null로 초기화
    }

    public HistoryChallengeResponse(HistoryChallenge challenge, HistoryChallengeData currentData) {
        this.challengeId = challenge.getChallengeId();
        this.theme = challenge.getTheme();
        this.stockName = challenge.getStockName();
        this.stockCode = challenge.getStockCode();
        this.startDate = challenge.getStartDate();
        this.endDate = challenge.getEndDate();
        this.currentPriceData = (currentData != null) ? new HistoryChallengeDataResponse(currentData) : null;
    }
}

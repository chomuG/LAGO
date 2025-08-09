package com.example.LAGO.dto.response;

import com.example.LAGO.domain.HistoryChallenge;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    // 생성자 추가
    public HistoryChallengeResponse(HistoryChallenge entity) {
        this.challengeId = entity.getChallengeId();
        this.theme = entity.getTheme();
        this.stockName = entity.getStockName();
        this.stockCode = entity.getStockCode();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
    }
}
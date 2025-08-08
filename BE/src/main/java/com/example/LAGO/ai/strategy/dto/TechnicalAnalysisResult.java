package com.example.LAGO.ai.strategy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 기술적 분석 결과 DTO
 * 
 * 주식의 기술적 분석 결과를 담는 DTO
 * 
 * @author D203팀 백엔드 개발자
 * @since 2025-08-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기술적 분석 결과")
public class TechnicalAnalysisResult {

    /**
     * 현재가
     */
    @Schema(description = "현재가", example = "70000")
    private Integer currentPrice;

    /**
     * 전일 대비 변동률
     */
    @Schema(description = "전일 대비 변동률", example = "2.5")
    private Double changeRate;

    /**
     * 5일 이동평균
     */
    @Schema(description = "5일 이동평균", example = "68500")
    private Double ma5;

    /**
     * 20일 이동평균
     */
    @Schema(description = "20일 이동평균", example = "67200")
    private Double ma20;

    /**
     * 60일 이동평균
     */
    @Schema(description = "60일 이동평균", example = "65800")
    private Double ma60;

    /**
     * RSI (14일)
     */
    @Schema(description = "RSI 지수", example = "72.5")
    private Double rsi;

    /**
     * MACD
     */
    @Schema(description = "MACD", example = "1250.5")
    private Double macd;

    /**
     * 볼린저 밴드 상단
     */
    @Schema(description = "볼린저 밴드 상단", example = "72500")
    private Double bollingerUpper;

    /**
     * 볼린저 밴드 하단
     */
    @Schema(description = "볼린저 밴드 하단", example = "62500")
    private Double bollingerLower;

    /**
     * 거래량 (최근 5일 평균 대비)
     */
    @Schema(description = "거래량 비율", example = "1.8")
    private Double volumeRatio;

    /**
     * 기술적 신호 종합 점수 (-1.0 ~ 1.0)
     */
    @Schema(description = "기술적 신호 점수", example = "0.6", minimum = "-1.0", maximum = "1.0")
    private Double technicalScore;

    /**
     * 기술적 신호 라벨
     */
    @Schema(description = "기술적 신호", example = "BULLISH", allowableValues = {"BULLISH", "BEARISH", "NEUTRAL"})
    private String technicalSignal;

    /**
     * 분석 기준일
     */
    @Schema(description = "분석 기준일")
    private LocalDate analysisDate;

    /**
     * 분석 요약
     */
    @Schema(description = "기술적 분석 요약", example = "RSI 과매수 구간이지만 이동평균선 상향 돌파로 상승 모멘텀 지속 예상")
    private String summary;
}

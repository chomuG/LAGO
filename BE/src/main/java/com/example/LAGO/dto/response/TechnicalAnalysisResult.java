package com.example.LAGO.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 기술적 분석 결과 Response DTO
 * 
 * 차트 패턴, RSI, MACD, 볼린저 밴드 등의 기술적 지표 분석 결과를 담는 응답 DTO
 * 
 * @author 라고할때 팀
 * @version 1.0
 * @since 2025-01-04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicalAnalysisResult {
    
    /** 종목 코드 */
    private String stockCode;
    
    /** 종목명 */
    private String stockName;
    
    /** 현재가 */
    private Float currentPrice;
    
    /** 변동률 */
    private Float fluctuationRate;
    
    /** RSI 지표 (0~100) */
    private Float rsi;
    
    /** MACD Line */
    private Float macdLine;
    
    /** MACD Signal Line */
    private Float signalLine;
    
    /** MACD Histogram */
    private Float macdHistogram;
    
    /** 볼린저 밴드 상단 */
    private Float bollingerUpperBand;
    
    /** 볼린저 밴드 중간 (이동평균) */
    private Float bollingerMiddleBand;
    
    /** 볼린저 밴드 하단 */
    private Float bollingerLowerBand;
    
    /** 5일 이동평균 */
    private Float ma5;
    
    /** 20일 이동평균 */
    private Float ma20;
    
    /** 60일 이동평균 */
    private Float ma60;
    
    /** 골든크로스 여부 */
    private boolean isGoldenCross;
    
    /** 데드크로스 여부 */
    private boolean isDeathCross;
    
    /** 종합 신호 (BUY/HOLD/SELL) */
    private String overallSignal;
    
    /** 신호 강도 (0~100) */
    private Integer signalStrength;
    
    /** 신호 근거 */
    private String signalReason;
    
    /** 분석 시간 */
    private LocalDateTime analysisTime;
    
    /** 거래량 */
    private Long volume;
    
    /** 시가총액 */
    private Long marketCap;
    
    /**
     * 매수 추천 결과 생성 팩토리 메서드
     */
    public static TechnicalAnalysisResult buyRecommendation(String stockCode, String stockName, 
                                                          Float currentPrice, Float fluctuationRate,
                                                          Float rsi, Integer signalStrength, String reason) {
        return TechnicalAnalysisResult.builder()
                .stockCode(stockCode)
                .stockName(stockName)
                .currentPrice(currentPrice)
                .fluctuationRate(fluctuationRate)
                .rsi(rsi)
                .overallSignal("BUY")
                .signalStrength(signalStrength)
                .signalReason(reason)
                .analysisTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 매도 추천 결과 생성 팩토리 메서드
     */
    public static TechnicalAnalysisResult sellRecommendation(String stockCode, String stockName,
                                                           Float currentPrice, Float fluctuationRate,
                                                           Float rsi, Integer signalStrength, String reason) {
        return TechnicalAnalysisResult.builder()
                .stockCode(stockCode)
                .stockName(stockName)
                .currentPrice(currentPrice)
                .fluctuationRate(fluctuationRate)
                .rsi(rsi)
                .overallSignal("SELL")
                .signalStrength(signalStrength)
                .signalReason(reason)
                .analysisTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 관망 추천 결과 생성 팩토리 메서드
     */
    public static TechnicalAnalysisResult holdRecommendation(String stockCode, String stockName,
                                                           Float currentPrice, Float fluctuationRate,
                                                           String reason) {
        return TechnicalAnalysisResult.builder()
                .stockCode(stockCode)
                .stockName(stockName)
                .currentPrice(currentPrice)
                .fluctuationRate(fluctuationRate)
                .overallSignal("HOLD")
                .signalStrength(0)
                .signalReason(reason)
                .analysisTime(LocalDateTime.now())
                .build();
    }
}

package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * AI 봇 거래 기록 엔티티
 * 지침서 명세 AI_BOT_TRADE 테이블과 완전 일치
 */
@Entity
@Table(name = "AI_BOT_TRADE")
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiBotTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bot_trade_id")
    private Long botTradeId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // AI 봇 사용자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id", nullable = false)
    private AiStrategy strategy; // 사용된 전략

    @Column(name = "stock_code", nullable = false, length = 10)
    private String stockCode; // 종목 코드

    @Column(name = "trade_type", nullable = false, length = 10)
    private String tradeType; // 거래 타입 (BUY/SELL)

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // 거래 수량

    @Column(name = "price", nullable = false)
    private Integer price; // 거래 단가

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount; // 총 거래 금액

    @Column(name = "result", nullable = false, length = 20)
    private String result; // 거래 결과 (SUCCESS/FAILED/PENDING)

    @Column(name = "profit_loss")
    private Integer profitLoss; // 실현 손익 (매도 시)

    @Column(name = "profit_loss_rate")
    private Float profitLossRate; // 실현 수익률 (매도 시)

    @Column(name = "log", columnDefinition = "TEXT")
    private String log; // 거래 로그 및 판단 근거

    @Column(name = "trade_time", nullable = false)
    private LocalDateTime tradeTime; // 거래 시간

    @Column(name = "signal_time", nullable = false)
    private LocalDateTime signalTime; // 매매 신호 발생 시간

    // 기술적 지표 값 (거래 당시)
    @Column(name = "rsi_value")
    private Float rsiValue; // RSI 값

    @Column(name = "macd_value")
    private Float macdValue; // MACD 값

    @Column(name = "bollinger_position")
    private String bollingerPosition; // 볼린저밴드 위치 (UPPER/MIDDLE/LOWER)

    @Column(name = "moving_average_5")
    private Integer movingAverage5; // 5일 이동평균

    @Column(name = "moving_average_20")
    private Integer movingAverage20; // 20일 이동평균

    @Column(name = "moving_average_60")
    private Integer movingAverage60; // 60일 이동평균

    // 매매 시점 시장 정보
    @Column(name = "market_price", nullable = false)
    private Integer marketPrice; // 시장가

    @Column(name = "volume", nullable = false)
    private Long volume; // 거래량

    @Column(name = "fluctuation_rate")
    private Float fluctuationRate; // 등락률

    @PrePersist
    protected void onCreate() {
        if (tradeTime == null) {
            tradeTime = LocalDateTime.now();
        }
        if (signalTime == null) {
            signalTime = LocalDateTime.now();
        }
        if (result == null) {
            result = "PENDING";
        }
        if (totalAmount == null && price != null && quantity != null) {
            totalAmount = price * quantity;
        }
    }

    /**
     * 매매 신호 생성 이유를 로그에 추가
     */
    public void addSignalLog(String reason, String indicators) {
        StringBuilder logBuilder = new StringBuilder();
        if (this.log != null) {
            logBuilder.append(this.log).append("\n");
        }
        
        logBuilder.append("[").append(LocalDateTime.now()).append("] ")
                 .append("Signal: ").append(reason).append("\n")
                 .append("Indicators: ").append(indicators);
        
        this.log = logBuilder.toString();
    }

    /**
     * 매도 시 손익 계산
     */
    public void calculateProfitLoss(Integer buyPrice) {
        if ("SELL".equals(this.tradeType) && buyPrice != null) {
            this.profitLoss = (this.price - buyPrice) * this.quantity;
            this.profitLossRate = ((float) (this.price - buyPrice) / buyPrice) * 100;
        }
    }
}

package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 모의거래 엔티티 (완전 구현)
 * 지침서 명세 MOCK_TRADE 테이블과 완전 일치
 */
@Entity
@Table(name = "\"MOCK_TRADE\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long tradeId;

    @Column(name = "account_id")
    private Integer accountId; // 계좌 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account; // 계좌 정보

    @Column(name = "stock_id", nullable = false)
    private Integer stockId; // STOCK_INFO 테이블의 stock_info_id 참조
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", insertable = false, updatable = false)
    private StockInfo stockInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "buy_sell", nullable = false)
    private TradeType tradeType; // 거래구분 (BUY/SELL)

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // 거래 수량

    @Column(name = "price", nullable = false)
    private Integer price; // 거래 단가

    @Column(name = "trade_at", nullable = false)
    private LocalDateTime tradeTime; // 거래 시간

    @Column(name = "commission")
    private Integer commission; // 수수료
    
    // 총 거래 금액 계산 메소드 (price * quantity 기반)
    public Integer getTotalAmount() {
        if (price != null && quantity != null) {
            return price * quantity;
        }
        return 0;
    }

    @PrePersist
    protected void onCreate() {
        if (tradeTime == null) {
            tradeTime = LocalDateTime.now();
        }
    }
}

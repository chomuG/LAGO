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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "stock_code", nullable = false, length = 10)
    private String stockCode; // 종목 코드

    @Column(name = "trade_type", nullable = false, length = 10)
    private String tradeType; // 거래구분 (BUY/SELL)

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // 거래 수량

    @Column(name = "price", nullable = false)
    private Integer price; // 거래 단가

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount; // 총 거래금액 (price * quantity)

    @Column(name = "trade_time", nullable = false)
    private LocalDateTime tradeTime; // 거래 시간

    @Column(name = "status", length = 20)
    private String status; // 거래 상태 (COMPLETED/PENDING/CANCELLED)

    @Column(name = "commission")
    private Integer commission; // 수수료

    @Column(name = "tax")
    private Integer tax; // 세금

    @PrePersist
    protected void onCreate() {
        if (tradeTime == null) {
            tradeTime = LocalDateTime.now();
        }
        if (status == null) {
            status = "COMPLETED";
        }
        if (totalAmount == null && price != null && quantity != null) {
            totalAmount = price * quantity;
        }
    }
}

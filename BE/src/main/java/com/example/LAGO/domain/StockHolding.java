package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 보유 주식 엔티티
 * STOCK_HOLDING 테이블과 매핑
 */
@Entity
@Table(name = "STOCK_HOLDING")
@Getter
@Setter
@NoArgsConstructor
public class StockHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long holdingId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "stock_info_id")
    private Long stockInfoId;

    @Column(name = "stock_code")
    private String stockCode;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "total_price")
    private Integer totalPrice;

    @Column(name = "average_price")
    private Integer averagePrice;

    @Column(name = "total_cost")
    private Integer totalCost;

    @Column(name = "current_value")
    private Integer currentValue;

    @Column(name = "profit_loss")
    private Integer profitLoss;

    @Column(name = "profit_loss_rate")
    private Double profitLossRate;

    @Column(name = "first_purchase_date")
    private LocalDateTime firstPurchaseDate;

    @Column(name = "last_trade_date")
    private LocalDateTime lastTradeDate;

    @Builder
    public StockHolding(Long accountId, Long stockInfoId, String stockCode, 
                       Integer quantity, Integer totalPrice) {
        this.accountId = accountId;
        this.stockInfoId = stockInfoId;
        this.stockCode = stockCode;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.averagePrice = totalPrice / quantity;
        this.totalCost = totalPrice;
        this.firstPurchaseDate = LocalDateTime.now();
        this.lastTradeDate = LocalDateTime.now();
    }

    /**
     * 현재 평가금액 업데이트
     */
    public void updateCurrentValue(Integer currentPrice) {
        if (currentPrice != null && this.quantity != null) {
            this.currentValue = currentPrice * this.quantity;
            this.profitLoss = this.currentValue - this.totalCost;
            if (this.totalCost != null && this.totalCost > 0) {
                this.profitLossRate = ((double) this.profitLoss / this.totalCost) * 100;
            }
        }
    }
}
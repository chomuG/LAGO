package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 보유주식 엔티티
 * 지침서 명세 STOCK_HOLDING 테이블과 완전 일치
 */
@Entity
@Table(name = "STOCK_HOLDING")
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long holdingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "stock_code", nullable = false, length = 10)
    private String stockCode; // 종목 코드

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // 보유 수량

    @Column(name = "average_price", nullable = false)
    private Integer averagePrice; // 평균 매수가

    @Column(name = "total_cost", nullable = false)
    private Integer totalCost; // 총 매수 금액 (수수료 포함)

    @Column(name = "current_value")
    private Integer currentValue; // 현재 평가금액

    @Column(name = "profit_loss")
    private Integer profitLoss; // 평가손익

    @Column(name = "profit_loss_rate")
    private Float profitLossRate; // 수익률

    @Column(name = "first_purchase_date", nullable = false)
    private LocalDateTime firstPurchaseDate; // 최초 매수일

    @Column(name = "last_trade_date", nullable = false)
    private LocalDateTime lastTradeDate; // 마지막 거래일

    @PrePersist
    protected void onCreate() {
        if (firstPurchaseDate == null) {
            firstPurchaseDate = LocalDateTime.now();
        }
        if (lastTradeDate == null) {
            lastTradeDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastTradeDate = LocalDateTime.now();
    }

    /**
     * 현재가를 기준으로 평가금액과 손익을 업데이트
     * @param currentPrice 현재 주가
     */
    public void updateCurrentValue(Integer currentPrice) {
        this.currentValue = currentPrice * this.quantity;
        this.profitLoss = this.currentValue - this.totalCost;
        this.profitLossRate = this.totalCost > 0 ? 
            ((float) this.profitLoss / this.totalCost) * 100 : 0.0f;
    }

    /**
     * 매수 시 보유 수량 및 평균가 업데이트
     * @param buyQuantity 매수 수량
     * @param buyPrice 매수 단가
     * @param commission 수수료
     */
    public void addStock(Integer buyQuantity, Integer buyPrice, Integer commission) {
        Integer newTotalCost = this.totalCost + (buyQuantity * buyPrice) + commission;
        Integer newQuantity = this.quantity + buyQuantity;
        
        this.averagePrice = newTotalCost / newQuantity;
        this.quantity = newQuantity;
        this.totalCost = newTotalCost;
        this.lastTradeDate = LocalDateTime.now();
    }

    /**
     * 매도 시 보유 수량 업데이트
     * @param sellQuantity 매도 수량
     * @param sellPrice 매도 단가
     * @param tax 세금
     */
    public void sellStock(Integer sellQuantity, Integer sellPrice, Integer tax) {
        if (sellQuantity > this.quantity) {
            throw new RuntimeException("보유 수량보다 많이 매도할 수 없습니다.");
        }
        
        // 매도한 비율만큼 총 매수 금액 차감
        Float sellRatio = (float) sellQuantity / this.quantity;
        Integer soldCost = (int) (this.totalCost * sellRatio);
        
        this.quantity -= sellQuantity;
        this.totalCost -= soldCost;
        this.lastTradeDate = LocalDateTime.now();
        
        // 전량 매도 시 평균가 초기화
        if (this.quantity == 0) {
            this.averagePrice = 0;
            this.totalCost = 0;
        }
    }
}

package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주식 분봉 데이터 엔티티
 * stock_minute 테이블과 매핑
 * 1분 단위 주식 가격 정보를 저장
 */
@Entity
@Table(name = "stock_minute")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMinute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "stock_code", nullable = false, length = 10)
    private String stockCode;

    @Column(name = "minute_datetime", nullable = false)
    private LocalDateTime minuteDateTime;

    @Column(name = "open_price", nullable = false)
    private Integer openPrice;

    @Column(name = "high_price", nullable = false)
    private Integer highPrice;

    @Column(name = "low_price", nullable = false)
    private Integer lowPrice;

    @Column(name = "close_price", nullable = false)
    private Integer closePrice;

    @Column(name = "volume", nullable = false)
    private Long volume;

    @Column(name = "trading_value")
    private Long tradingValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

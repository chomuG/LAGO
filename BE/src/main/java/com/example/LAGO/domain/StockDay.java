package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

/**
 * 주식 일봉 데이터 엔티티
 * 연동된 EC2 DB STOCK_DAY 테이블과 완전 일치
 * 
 * 테이블 구조:
 * - stock_day_id: PK
 * - stock_info_id: FK (STOCK_INFO)
 * - date: 거래일
 * - open_price, high_price, low_price, close_price: OHLC 데이터
 * - fluctuation_rate: 등락률
 * - volume: 거래량
 */
@Entity
@Table(name = "STOCK_DAY")
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDay {

    /**
     * 주식 일봉 데이터 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_day_id")
    private Integer stockDayId;

    /**
     * 종목 정보 ID (FK)
     * STOCK_INFO 테이블과 연관관계
     */
    @Column(name = "stock_info_id", nullable = false)
    private Integer stockInfoId;

    /**
     * 거래일
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * 시가 (원)
     */
    @Column(name = "open_price", nullable = false)
    private Integer openPrice;

    /**
     * 고가 (원)
     */
    @Column(name = "high_price", nullable = false)
    private Integer highPrice;

    /**
     * 저가 (원)
     */
    @Column(name = "low_price", nullable = false)
    private Integer lowPrice;

    /**
     * 종가 (원)
     */
    @Column(name = "close_price", nullable = false)
    private Integer closePrice;

    /**
     * 등락률 (%)
     */
    @Column(name = "fluctuation_rate")
    private Float fluctuationRate;

    /**
     * 거래량 (주)
     */
    @Column(name = "volume", nullable = false)
    private Integer volume;

    /**
     * STOCK_INFO와의 다대일 관계
     * 지연 로딩 사용으로 성능 최적화
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_info_id", referencedColumnName = "stock_info_id", insertable = false, updatable = false)
    private StockInfo stockInfo;
}

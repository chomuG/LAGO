package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "\"STOCK_MINUTE\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMinute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_mid_id")
    private Integer stockMidId;

    // 외래키
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_info_id")
    private StockInfo stockInfo;

    @Column(name = "date", nullable = false)
    private java.time.LocalDateTime date;

    @Column(name = "open_price", nullable = false)
    private Integer openPrice;

    @Column(name = "high_price", nullable = false)
    private Integer highPrice;

    @Column(name = "low_price", nullable = false)
    private Integer lowPrice;

    @Column(name = "close_price", nullable = false)
    private Integer closePrice;

    @Column(name = "volume", nullable = false)
    private Integer volume;
}
package com.example.LAGO.domain;

import jakarta.persistence.*;
<<<<<<< HEAD
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 주식 분단위 데이터 엔티티
 * 지침서 명세: 연동된 EC2 DB STOCK_MINUTE 테이블과 완전 일치
 * 
 * 연동된 EC2 DB 테이블 구조 (추정):
 * - stock_minute_id: PK (int, auto_increment)
 * - stock_info_id: FK (int) -> STOCK_INFO 테이블
 * - date_time: 거래 일시 (datetime) - 분단위
 * - open_price: 시가 (int)
 * - high_price: 고가 (int)
 * - low_price: 저가 (int)
 * - close_price: 종가 (int)
 * - volume: 거래량 (bigint)
 * - fluctuation_rate: 등락률 (float)
 * 
 * 실시간 차트 및 기술적 분석에 사용
 * Java 21 Virtual Thread를 활용한 대량 데이터 처리 최적화
 * 
 * @author D203팀 백엔드 개발자
 * @since 2025-08-06
 */
@Entity
@Table(name = "STOCK_MINUTE", indexes = {
    @Index(name = "idx_stock_minute_stock_info_id", columnList = "stock_info_id"),
    @Index(name = "idx_stock_minute_date_time", columnList = "date_time"),
    @Index(name = "idx_stock_minute_stock_date", columnList = "stock_info_id, date_time")
})
@Getter 
=======
import lombok.*;

@Entity
@Table(name = "\"STOCK_MINUTE\"")
@Getter
>>>>>>> origin/backend-dev
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMinute {
<<<<<<< HEAD

    /**
     * 주식 분단위 데이터 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_minute_id")
    private Integer stockMinuteId;

    /**
     * 주식 정보 ID (FK)
     * STOCK_INFO 테이블 참조
     */
    @Column(name = "stock_info_id", nullable = false)
    private Integer stockInfoId;

    import lombok.*;
    private LocalDateTime dateTime;

    /**
     * 시가 (해당 분의 첫 거래 가격)
     */
    @Column(name = "open_price", nullable = false)
    private Integer openPrice;

    /**
     * 고가 (해당 분의 최고 거래 가격)
     */
    @Column(name = "high_price", nullable = false)
    private Integer highPrice;

    /**
     * 저가 (해당 분의 최저 거래 가격)
     */
    @Column(name = "low_price", nullable = false)
    private Integer lowPrice;

    /**
     * 종가 (해당 분의 마지막 거래 가격)
     */
    @Column(name = "close_price", nullable = false)
    private Integer closePrice;

    /**
     * 거래량 (해당 분 동안의 총 거래량)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Column(name = "volume", nullable = false)
    private Long volume;

    /**
     * 등락률 (전일 종가 대비 %)
     */
    @Column(name = "fluctuation_rate")
    private Float fluctuationRate;

    /**
     * 생성 시간 자동 설정
     */
    @PrePersist
    protected void onCreate() {
        if (dateTime == null) {
            dateTime = LocalDateTime.now();
        }
    }

    /**
     * 등락률 계산
     * 
     * @param previousClosePrice 이전 종가
     */
    public void calculateFluctuationRate(Integer previousClosePrice) {
        if (previousClosePrice != null && previousClosePrice > 0) {
            this.fluctuationRate = ((float) (closePrice - previousClosePrice) / previousClosePrice) * 100;
        } else {
            this.fluctuationRate = 0.0f;
        }
    }

    /**
     * 캔들스틱 데이터 유효성 검증
     * 
     * @return 유효한 캔들스틱 데이터인지 여부
     */
    public boolean isValidCandlestick() {
        return openPrice != null && highPrice != null && 
               lowPrice != null && closePrice != null &&
               lowPrice <= openPrice && lowPrice <= closePrice &&
               highPrice >= openPrice && highPrice >= closePrice &&
               volume != null && volume >= 0;
    }

    /**
     * 상승 캔들인지 확인
     * 
     * @return 상승 캔들 여부 (종가 > 시가)
     */
    public boolean isBullishCandle() {
        return closePrice != null && openPrice != null && closePrice > openPrice;
    }

    /**
     * 하락 캔들인지 확인
     * 
     * @return 하락 캔들 여부 (종가 < 시가)
     */
    public boolean isBearishCandle() {
        return closePrice != null && openPrice != null && closePrice < openPrice;
    }

    /**
     * 도지 캔들인지 확인 (시가와 종가가 같거나 매우 비슷)
     * 
     * @return 도지 캔들 여부
     */
    public boolean isDojiCandle() {
        if (closePrice == null || openPrice == null) return false;
        
        float bodyRatio = Math.abs((float)(closePrice - openPrice) / openPrice) * 100;
        return bodyRatio < 0.1f; // 0.1% 미만 차이면 도지로 판단
    }

    /**
     * 거래량 급증 여부 확인
     * 
     * @param averageVolume 평균 거래량
     * @param threshold 급증 임계값 (배수)
     * @return 거래량 급증 여부
     */
    public boolean hasVolumeSpike(Long averageVolume, Double threshold) {
        if (volume == null || averageVolume == null || averageVolume == 0) {
            return false;
        }
        
        double volumeRatio = (double) volume / averageVolume;
        return volumeRatio >= threshold;
    }

    /**
     * 실체 크기 계산 (시가와 종가의 차이)
     * 
     * @return 실체 크기
     */
    public Integer getCandleBodySize() {
        if (openPrice == null || closePrice == null) return 0;
        return Math.abs(closePrice - openPrice);
    }

    /**
     * 위꼬리 크기 계산 (고가에서 시가/종가 중 높은 값의 차이)
     * 
     * @return 위꼬리 크기
     */
    public Integer getUpperShadowSize() {
        if (highPrice == null || openPrice == null || closePrice == null) return 0;
        int bodyTop = Math.max(openPrice, closePrice);
        return highPrice - bodyTop;
    }

    /**
     * 아래꼬리 크기 계산 (시가/종가 중 낮은 값에서 저가의 차이)
     * 
     * @return 아래꼬리 크기
     */
    public Integer getLowerShadowSize() {
        if (lowPrice == null || openPrice == null || closePrice == null) return 0;
        int bodyBottom = Math.min(openPrice, closePrice);
        return bodyBottom - lowPrice;
    }
=======
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_mid_id")
    private Integer stockMidId;

    // 외래키
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_info_id")
    private StockInfo stockInfo;

    @Column(name = "date", nullable = false)
    private java.time.LocalDateTime date;

    @Column(name = "open_price", nullable = false)
    private Integer openPrice;

    @Column(name = "high_price", nullable = false)
    private Integer highPrice;

    @Column(name = "low_price", nullable = false)
    private Integer lowPrice;

    @Column(name = "close_price", nullable = false)
    private Integer closePrice;

    @Column(name = "volume", nullable = false)
    private Integer volume;
>>>>>>> origin/backend-dev
}

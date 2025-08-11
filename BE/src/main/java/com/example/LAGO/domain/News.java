package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "NEWS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    @Column(nullable = false, length = 1000)
    private String url;
    
    
    @Column(name = "published_date")
    private LocalDateTime publishedDate;
    
    @Column(length = 200)
    private String source;
    
    // 종목 정보 연결 (기존 STOCK_INFO 테이블과 관계)
    @Column(name = "stock_code", length = 20)
    private String stockCode;
    
    // DB 스키마에 맞춰 추가된 필드들
    @Column(length = 200)
    private String keywords;
    
    @Column(name = "stock_info_id")
    private Long stockInfoId;
    
    @Column(name = "stock_name", length = 200)
    private String stockName;
    
    // 수집 타입 (REALTIME, WATCHLIST, HISTORICAL)
    @Column(name = "collection_type", length = 50)
    private String collectionType;
    
    // FinBERT 감정분석 결과 (핵심만)
    @Column(length = 20)
    private String sentiment;  // 호재악재 표시용: POSITIVE, NEGATIVE, NEUTRAL
    
    @Column(name = "confidence_level", length = 20)
    private String confidenceLevel;  // 신뢰도: very_high, high, medium, low
    
    @Column(name = "trading_signal", length = 20)
    private String tradingSignal;  // 거래신호: BUY, WEAK_BUY, HOLD, WEAK_SELL, SELL
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
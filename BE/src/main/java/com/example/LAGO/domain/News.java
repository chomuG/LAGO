package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.*;
import com.example.LAGO.converter.StringListConverter;

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
    @Column(name = "news_id")
    private Long newsId;
    
    @Column(columnDefinition = "TEXT")
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    @Column(columnDefinition = "TEXT")
    private String sentiment;  // 감정 분석 결과
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(columnDefinition = "TEXT")
    private String type;  // 뉴스 타입
    
    // Additional fields expected by the service
    @Column(name = "published_date")
    private LocalDateTime publishedDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(columnDefinition = "TEXT")
    private String url;
    
    @Column(columnDefinition = "TEXT")
    private String source;
    
    @Column(name = "stock_code")
    private String stockCode;
    
    @Column(name = "confidence_level")
    private String confidenceLevel;
    
    @Column(name = "trading_signal")
    private String tradingSignal;
    
    @Column(columnDefinition = "TEXT")
    private String keywords;
    
    @Column(name = "collection_type")
    private String collectionType;
    
    @Column(name = "stock_info_id")
    private Long stockInfoId;
    
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> images;
    
    // Stock relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_info_id", insertable = false, updatable = false)
    private StockInfo stock;
    
    // Getter method for ID (for backward compatibility)
    public Long getId() {
        return newsId;
    }
    
    // Getter method for stock name
    public String getStockName() {
        return stock != null ? stock.getCompanyName() : null;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
        if (publishedDate == null) {
            publishedDate = publishedAt;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
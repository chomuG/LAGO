package com.example.LAGO.dto.response;

import com.example.LAGO.domain.News;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponse {
    
    private Long id;
    private String title;
    private String summary;
    private String url;
    private List<String> images;
    private LocalDateTime publishedDate;
    private String source;
    private String stockCode;
    private String stockName;
    private String keywords;
    private String sentiment;
    private LocalDateTime createdAt;
    
    public static NewsResponse from(News news) {
        return NewsResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .summary(news.getSummary())
                .url(news.getUrl())
                .images(news.getImages())
                .publishedDate(news.getPublishedDate())
                .source(news.getSource())
                .stockCode(news.getStockCode())
                .stockName(news.getStockName())
                .keywords(news.getKeywords())
                .sentiment(news.getSentiment())
                .createdAt(news.getCreatedAt())
                .build();
    }
}
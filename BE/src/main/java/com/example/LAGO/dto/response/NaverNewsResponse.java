package com.example.LAGO.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class NaverNewsResponse {
    
    @JsonProperty("lastBuildDate")
    private String lastBuildDate;
    
    @JsonProperty("total")
    private Integer total;
    
    @JsonProperty("start")
    private Integer start;
    
    @JsonProperty("display")
    private Integer display;
    
    @JsonProperty("items")
    private List<NewsItem> items;
    
    @Data
    public static class NewsItem {
        @JsonProperty("title")
        private String title;
        
        @JsonProperty("originallink")
        private String originalLink;
        
        @JsonProperty("link")
        private String link;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("pubDate")
        private String pubDate;
    }
}
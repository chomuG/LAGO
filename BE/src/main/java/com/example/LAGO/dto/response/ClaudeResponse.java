package com.example.LAGO.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ClaudeResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("role")
    private String role;
    
    @JsonProperty("content")
    private List<Content> content;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("stop_reason")
    private String stopReason;
    
    @JsonProperty("stop_sequence")
    private String stopSequence;
    
    @JsonProperty("usage")
    private Usage usage;
    
    @Data
    public static class Content {
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("text")
        private String text;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)  // 알 수 없는 필드 무시 (service_tier 등)
    public static class Usage {
        @JsonProperty("input_tokens")
        private Integer inputTokens;
        
        @JsonProperty("output_tokens")
        private Integer outputTokens;
        
        @JsonProperty("cache_creation_input_tokens")
        private Integer cacheCreationInputTokens;
        
        @JsonProperty("cache_read_input_tokens")
        private Integer cacheReadInputTokens;
    }
}
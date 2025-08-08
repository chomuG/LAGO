package com.example.LAGO.dto.response;

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
    public static class Usage {
        @JsonProperty("input_tokens")
        private Integer inputTokens;
        
        @JsonProperty("output_tokens")
        private Integer outputTokens;
    }
}
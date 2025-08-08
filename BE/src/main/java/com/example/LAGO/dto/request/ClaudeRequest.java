package com.example.LAGO.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaudeRequest {
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    
    @JsonProperty("messages")
    private List<Message> messages;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        @JsonProperty("role")
        private String role;
        
        @JsonProperty("content")
        private String content;
    }
}
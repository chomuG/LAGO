package com.example.LAGO.realtime.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class TickPushDto {
    private String code;          // 종목코드
    private String date;          // HHmmss (원본 그대로)
    private Integer openPrice;
    private Integer highPrice;
    private Integer lowPrice;
    private Integer closePrice;
    private Integer volume;
    private LocalDateTime receivedAt; // 서버 수신시각
}

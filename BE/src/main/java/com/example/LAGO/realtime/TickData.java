package com.example.LAGO.realtime;

// 틱 데이터 임시 데이터 컨테이너
// 일종의 DTO 역할

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TickData {
    private String code;      // 종목코드
    private String date;      // HHMMSS 형태
    private Integer closePrice;  // 현재가
    private Integer openPrice;   // 시가
    private Integer highPrice;   // 고가
    private Integer lowPrice;    // 저가
    private Integer volume;        // 거래량

    @Builder.Default
    private LocalDateTime receivedAt = LocalDateTime.now();

    // HHMMSS를 LocalDateTime으로 변환
    // 제대로 된 값이 아니면 null 반환
    public LocalDateTime getParsedDateTime() {
        try {
            LocalTime time = LocalTime.parse(date, DateTimeFormatter.ofPattern("HHmmss"));
            return LocalDateTime.now().toLocalDate().atTime(time);
        } catch (Exception e) {
            return null;
        }
    }

    // 분 단위로 자르기 (1분봉 집계용)
    public LocalDateTime getTruncatedMinute() {
        LocalDateTime dt = getParsedDateTime();
        return dt != null ? dt.withSecond(0).withNano(0) : null;
    }

    // 종목 + 날짜 + 분 > 고유키 반환 (문자열 기반 식별시 사용)
    public String getMinuteKey() {
        LocalDateTime dt = getTruncatedMinute();
        return (code != null ? code : "unknown") + "_" +
                (dt != null ? dt.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) : "invalid");
    }

    // 장 시간(9시 ~ 15시)
    private boolean isMarketTime(LocalTime time) {
        LocalTime marketOpen = LocalTime.of(9, 0);
        LocalTime marketClose = LocalTime.of(15, 30);
        return !time.isBefore(marketOpen) && !time.isAfter(marketClose);
    }

    // 유효성 검사
    public boolean isValid() {
        return code != null && !code.isEmpty()
                && date != null && date.matches("\\d{6}") // HHmmss 6자리
                && closePrice != null && closePrice >= 0
                && openPrice != null && openPrice >= 0
                && highPrice != null && highPrice >= 0
                && lowPrice != null && lowPrice >= 0
                && volume != null && volume > 0 // 거래가 성사된거니 거래량은 0보다 커야함
                && getParsedDateTime() != null; // 날짜 파싱 성공 여부
    }


}

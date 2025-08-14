package com.example.LAGO.dto;

import com.example.LAGO.domain.TradeType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주문 처리 결과 DTO
 * Redis Stream에서 주문 처리 완료 후 결과를 담는 구조
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResultDto {
    
    /**
     * 원본 주문 ID
     */
    private String orderId;
    
    /**
     * 처리 성공 여부
     */
    private boolean success;
    
    /**
     * 사용자 ID
     */
    private Long userId;
    
    /**
     * 종목 코드
     */
    private String stockCode;
    
    /**
     * 종목명
     */
    private String stockName;
    
    /**
     * 거래 타입
     */
    private TradeType tradeType;
    
    /**
     * 체결 수량
     */
    private Integer executedQuantity;
    
    /**
     * 체결 가격
     */
    private Integer executedPrice;
    
    /**
     * 총 거래 금액
     */
    private Integer totalAmount;
    
    /**
     * 수수료
     */
    private Integer commission;
    
    /**
     * 세금 (매도시)
     */
    private Integer tax;
    
    /**
     * 거래 후 잔액
     */
    private Integer remainingBalance;
    
    /**
     * 생성된 거래 ID (mock_trade 테이블)
     */
    private Long tradeId;
    
    /**
     * 처리 시간
     */
    private LocalDateTime processedAt;
    
    /**
     * 결과 메시지
     */
    private String message;
    
    /**
     * 오류 코드 (실패시)
     */
    private String errorCode;
    
    /**
     * 오류 상세 (실패시)
     */
    private String errorDetail;
    
    /**
     * 성공 응답 생성
     */
    public static OrderResultDto success(String orderId, Long userId, String stockCode, String stockName,
                                       TradeType tradeType, Integer executedQuantity, Integer executedPrice,
                                       Integer totalAmount, Integer commission, Integer tax, 
                                       Integer remainingBalance, Long tradeId, String message) {
        return OrderResultDto.builder()
                .orderId(orderId)
                .success(true)
                .userId(userId)
                .stockCode(stockCode)
                .stockName(stockName)
                .tradeType(tradeType)
                .executedQuantity(executedQuantity)
                .executedPrice(executedPrice)
                .totalAmount(totalAmount)
                .commission(commission)
                .tax(tax)
                .remainingBalance(remainingBalance)
                .tradeId(tradeId)
                .processedAt(LocalDateTime.now())
                .message(message)
                .build();
    }
    
    /**
     * 실패 응답 생성
     */
    public static OrderResultDto failure(String orderId, Long userId, String stockCode,
                                       String errorCode, String errorDetail) {
        return OrderResultDto.builder()
                .orderId(orderId)
                .success(false)
                .userId(userId)
                .stockCode(stockCode)
                .processedAt(LocalDateTime.now())
                .errorCode(errorCode)
                .errorDetail(errorDetail)
                .message("주문 처리 실패: " + errorDetail)
                .build();
    }
    
    /**
     * Redis Stream 결과 발행용 Map 변환
     */
    public java.util.Map<String, String> toStreamFields() {
        java.util.Map<String, String> fields = new java.util.HashMap<>();
        
        fields.put("orderId", orderId);
        fields.put("success", String.valueOf(success));
        fields.put("userId", String.valueOf(userId));
        fields.put("stockCode", stockCode);
        
        if (stockName != null) fields.put("stockName", stockName);
        if (tradeType != null) fields.put("tradeType", tradeType.name());
        if (executedQuantity != null) fields.put("executedQuantity", String.valueOf(executedQuantity));
        if (executedPrice != null) fields.put("executedPrice", String.valueOf(executedPrice));
        if (totalAmount != null) fields.put("totalAmount", String.valueOf(totalAmount));
        if (commission != null) fields.put("commission", String.valueOf(commission));
        if (tax != null) fields.put("tax", String.valueOf(tax));
        if (remainingBalance != null) fields.put("remainingBalance", String.valueOf(remainingBalance));
        if (tradeId != null) fields.put("tradeId", String.valueOf(tradeId));
        if (processedAt != null) fields.put("processedAt", processedAt.toString());
        if (message != null) fields.put("message", message);
        if (errorCode != null) fields.put("errorCode", errorCode);
        if (errorDetail != null) fields.put("errorDetail", errorDetail);
        
        return fields;
    }
}
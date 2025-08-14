package com.example.LAGO.dto;

import com.example.LAGO.domain.TradeType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Redis Stream 주문 처리용 DTO
 * 매수/매도 주문을 비동기로 처리하기 위한 데이터 구조
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    
    /**
     * 주문 고유 ID (Redis Stream Record ID와 매핑)
     */
    private String orderId;
    
    /**
     * 사용자 ID
     */
    private Long userId;
    
    /**
     * 종목 코드 (예: "005930")
     */
    private String stockCode;
    
    /**
     * 거래 타입 (BUY/SELL)
     */
    private TradeType tradeType;
    
    /**
     * 주문 수량
     */
    private Integer quantity;
    
    /**
     * 주문 가격 (null이면 시장가)
     */
    private Integer price;
    
    /**
     * 계좌 ID (옵션, null이면 기본 계좌)
     */
    private Long accountId;
    
    /**
     * 주문 생성 시간
     */
    private LocalDateTime orderTime;
    
    /**
     * 주문 상태
     */
    private OrderStatus status;
    
    /**
     * 주문 우선순위 (높을수록 우선 처리)
     */
    private Integer priority;
    
    /**
     * 주문 상태 열거형
     */
    public enum OrderStatus {
        PENDING,    // 대기중
        PROCESSING, // 처리중  
        COMPLETED,  // 완료
        FAILED,     // 실패
        CANCELLED   // 취소
    }
    
    /**
     * Redis Stream에 저장할 Map 형태로 변환
     */
    public java.util.Map<String, String> toStreamFields() {
        java.util.Map<String, String> fields = new java.util.HashMap<>();
        
        fields.put("userId", String.valueOf(userId));
        fields.put("stockCode", stockCode);
        fields.put("tradeType", tradeType.name());
        fields.put("quantity", String.valueOf(quantity));
        
        if (price != null) {
            fields.put("price", String.valueOf(price));
        }
        if (accountId != null) {
            fields.put("accountId", String.valueOf(accountId));
        }
        if (orderTime != null) {
            fields.put("orderTime", orderTime.toString());
        }
        if (status != null) {
            fields.put("status", status.name());
        }
        if (priority != null) {
            fields.put("priority", String.valueOf(priority));
        }
        
        return fields;
    }
    
    /**
     * Redis Stream Map 데이터에서 OrderDto 생성
     */
    public static OrderDto fromStreamFields(String recordId, java.util.Map<Object, Object> fields) {
        try {
            OrderDtoBuilder builder = OrderDto.builder();
            
            builder.orderId(recordId);
            
            // 필수 필드
            builder.userId(Long.valueOf(fields.get("userId").toString()));
            builder.stockCode(fields.get("stockCode").toString());
            builder.tradeType(TradeType.valueOf(fields.get("tradeType").toString()));
            builder.quantity(Integer.valueOf(fields.get("quantity").toString()));
            
            // 선택 필드
            if (fields.containsKey("price")) {
                builder.price(Integer.valueOf(fields.get("price").toString()));
            }
            if (fields.containsKey("accountId")) {
                builder.accountId(Long.valueOf(fields.get("accountId").toString()));
            }
            if (fields.containsKey("orderTime")) {
                builder.orderTime(LocalDateTime.parse(fields.get("orderTime").toString()));
            }
            if (fields.containsKey("status")) {
                builder.status(OrderStatus.valueOf(fields.get("status").toString()));
            }
            if (fields.containsKey("priority")) {
                builder.priority(Integer.valueOf(fields.get("priority").toString()));
            }
            
            return builder.build();
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid stream fields for OrderDto: " + e.getMessage(), e);
        }
    }
    
    /**
     * 주문의 유효성 검증
     */
    public boolean isValid() {
        return userId != null && userId > 0
            && stockCode != null && !stockCode.trim().isEmpty()
            && tradeType != null
            && quantity != null && quantity > 0
            && (price == null || price > 0); // 시장가는 null 허용
    }
}
package com.example.LAGO.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 매매 응답 DTO
 * 지침서 명세: 매매 완료 후 응답
 */
@Data
@Builder
@Schema(description = "매매 응답")
public class TradeResponse {

    @Schema(description = "거래 ID", example = "12345")
    private Long tradeId;

    @Schema(description = "종목 코드", example = "005930")
    private String stockCode;

    @Schema(description = "종목명", example = "삼성전자")
    private String stockName;

    @Schema(description = "거래 타입", example = "BUY")
    private String tradeType;

    @Schema(description = "거래 수량", example = "10")
    private Integer quantity;

    @Schema(description = "거래 단가", example = "75000")
    private Integer price;

    @Schema(description = "총 거래금액", example = "750000")
    private Integer totalAmount;

    @Schema(description = "수수료", example = "1500")
    private Integer commission;

    @Schema(description = "세금", example = "750")
    private Integer tax;

    @Schema(description = "거래 시간", example = "2025-08-05T09:30:00")
    private LocalDateTime tradeTime;

    @Schema(description = "거래 상태", example = "COMPLETED")
    private String status;

    @Schema(description = "계좌 잔액 (거래 후)", example = "1248750")
    private Integer remainingBalance;

    @Schema(description = "결과 메시지", example = "매수 주문이 체결되었습니다")
    private String message;
    
    @Schema(description = "에러 코드 (실패시)", example = "INSUFFICIENT_BALANCE")
    private String errorCode;
    
    @Schema(description = "성공 여부", example = "true")
    private boolean success;
    
    /**
     * 성공 응답 생성 팩토리 메서드
     * 지침서 명세: 예외처리/Validation 코드 필수
     */
    public static TradeResponse success(Long tradeId, Integer userId, String stockCode, String stockName,
                                      String tradeType, Integer quantity, Integer price, Integer totalAmount,
                                      Integer commission, Integer tax, Integer remainingBalance, String message) {
        return TradeResponse.builder()
                .success(true)
                .tradeId(tradeId)
                .stockCode(stockCode)
                .stockName(stockName)
                .tradeType(tradeType)
                .quantity(quantity)
                .price(price)
                .totalAmount(totalAmount)
                .commission(commission)
                .tax(tax)
                .remainingBalance(remainingBalance)
                .tradeTime(LocalDateTime.now())
                .message(message)
                .build();
    }

    /**
     * 실패 응답 생성 팩토리 메서드
     */
    public static TradeResponse failure(String errorCode, String message) {
        return TradeResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .tradeTime(LocalDateTime.now())
                .build();
    }

    /**
     * 실패 응답 생성 (상세 정보 포함)
     */
    public static TradeResponse failure(Integer userId, String stockCode, String tradeType, 
                                      String errorCode, String message) {
        return TradeResponse.builder()
                .success(false)
                .stockCode(stockCode)
                .tradeType(tradeType)
                .errorCode(errorCode)
                .message(message)
                .tradeTime(LocalDateTime.now())
                .build();
    }
}

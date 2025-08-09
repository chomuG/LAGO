package com.example.LAGO.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 매매 요청
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매매 요청")
public class TradeRequest {

    @Schema(description = "종목 코드", example = "005930", required = true)
    @NotBlank(message = "종목 코드는 필수입니다")
    private String stockCode;

    @Schema(description = "거래 타입 (BUY/SELL)", example = "BUY", required = true)
    @NotBlank(message = "거래 타입은 필수입니다")
    private String tradeType;

    @Schema(description = "거래 수량", example = "10", required = true)
    @NotNull(message = "거래 수량은 필수입니다")
    @Positive(message = "거래 수량은 양수여야 합니다")
    private Integer quantity;

    @Schema(description = "거래 단가", example = "75000", required = true)
    @NotNull(message = "거래 단가는 필수입니다")
    @Positive(message = "거래 단가는 양수여야 합니다")
    private Integer price;

    @Schema(description = "계좌 ID", example = "1001")
    private Integer accountId; // 선택적 - 없으면 기본 계좌 사용
}
package com.example.LAGO.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
<<<<<<< HEAD
import jakarta.validation.constraints.Min;
=======
>>>>>>> origin/backend-dev
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 매매 요청 DTO
<<<<<<< HEAD
 * 지침서 명세: API 명세서 기준, URL/메서드/입출력 모두 일치 (파라미터/반환 구조 임의 변경 금지)
 * 
 * 사용 API:
 * - POST /api/stocks/buy: 매수 주문
 * - POST /api/stocks/sell: 매도 주문
 * 
 * 연동된 EC2 DB 테이블:
 * - MOCK_TRADE: 거래 내역 저장
 * - ACCOUNT: 계좌 잔액 업데이트
 * - STOCK_HOLDING: 보유 주식 관리
 * 
 * @author D203팀 백엔드 개발자
 * @since 2025-08-06
=======
 * 지침서 명세: 사용자 매매 요청 (POST /api/stocks/buy, /api/stocks/sell)
>>>>>>> origin/backend-dev
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
<<<<<<< HEAD
@Schema(description = "매매 요청 정보")
public class TradeRequest {

    /**
     * 종목 코드 (6자리)
     * 예: 005930 (삼성전자), 000660 (SK하이닉스)
     */
=======
@Schema(description = "매매 요청")
public class TradeRequest {

>>>>>>> origin/backend-dev
    @Schema(description = "종목 코드", example = "005930", required = true)
    @NotBlank(message = "종목 코드는 필수입니다")
    private String stockCode;

<<<<<<< HEAD
    /**
     * 거래 타입
     * - "BUY": 매수
     * - "SELL": 매도
     */
    @Schema(description = "거래 타입", example = "BUY", allowableValues = {"BUY", "SELL"}, required = true)
    @NotBlank(message = "거래 타입은 필수입니다")
    private String tradeType;

    /**
     * 거래 수량 (주)
     * 최소 1주 이상
     */
    @Schema(description = "거래 수량", example = "10", required = true)
    @NotNull(message = "거래 수량은 필수입니다")
    @Positive(message = "거래 수량은 1주 이상이어야 합니다")
    private Integer quantity;

    /**
     * 거래 단가 (원)
     * 시장가 주문: null 또는 0
     * 지정가 주문: 구체적인 가격 지정
     */
    @Schema(description = "거래 단가 (시장가 주문시 null 가능)", example = "75000")
    @Min(value = 0, message = "거래 단가는 0 이상이어야 합니다")
    private Integer price;

    /**
     * 계좌 ID (선택적)
     * 지정하지 않으면 사용자의 기본 계좌 사용
     */
    @Schema(description = "계좌 ID (지정하지 않으면 기본 계좌 사용)", example = "1001")
    private Integer accountId;

    /**
     * 주문 타입
     * - "MARKET": 시장가 주문 (즉시 체결)
     * - "LIMIT": 지정가 주문 (특정 가격에서 체결)
     */
    @Schema(description = "주문 타입", example = "MARKET", allowableValues = {"MARKET", "LIMIT"})
    @Builder.Default
    private String orderType = "MARKET";

    /**
     * 매수 요청인지 확인
     * 
     * @return 매수 요청 여부
     */
    public boolean isBuyOrder() {
        return "BUY".equalsIgnoreCase(tradeType);
    }

    /**
     * 매도 요청인지 확인
     * 
     * @return 매도 요청 여부
     */
    public boolean isSellOrder() {
        return "SELL".equalsIgnoreCase(tradeType);
    }

    /**
     * 시장가 주문인지 확인
     * 
     * @return 시장가 주문 여부
     */
    public boolean isMarketOrder() {
        return "MARKET".equalsIgnoreCase(orderType);
    }

    /**
     * 지정가 주문인지 확인
     * 
     * @return 지정가 주문 여부
     */
    public boolean isLimitOrder() {
        return "LIMIT".equalsIgnoreCase(orderType);
    }

    /**
     * 요청 유효성 검증
     * 
     * @return 유효한 요청인지 여부
     */
    public boolean isValid() {
        // 기본 필드 검증
        if (stockCode == null || stockCode.trim().isEmpty()) {
            return false;
        }
        
        if (tradeType == null || (!isBuyOrder() && !isSellOrder())) {
            return false;
        }
        
        if (quantity == null || quantity <= 0) {
            return false;
        }
        
        // 지정가 주문인 경우 가격 필수
        if (isLimitOrder() && (price == null || price <= 0)) {
            return false;
        }
        
        return true;
    }

    /**
     * 총 거래금액 계산 (수수료 제외)
     * 
     * @param actualPrice 실제 체결 가격 (시장가 주문의 경우)
     * @return 총 거래금액
     */
    public Integer calculateTotalAmount(Integer actualPrice) {
        Integer tradePrice = isMarketOrder() ? actualPrice : this.price;
        
        if (tradePrice == null || tradePrice <= 0) {
            return 0;
        }
        
        return tradePrice * quantity;
    }

    /**
     * 매수 요청 생성 헬퍼 메서드
     * 
     * @param stockCode 종목 코드
     * @param quantity 매수 수량
     * @param price 매수 가격 (null이면 시장가)
     * @return 매수 요청 DTO
     */
    public static TradeRequest createBuyRequest(String stockCode, Integer quantity, Integer price) {
        return TradeRequest.builder()
                .stockCode(stockCode)
                .tradeType("BUY")
                .quantity(quantity)
                .price(price)
                .orderType(price == null ? "MARKET" : "LIMIT")
                .build();
    }

    /**
     * 매도 요청 생성 헬퍼 메서드
     * 
     * @param stockCode 종목 코드
     * @param quantity 매도 수량
     * @param price 매도 가격 (null이면 시장가)
     * @return 매도 요청 DTO
     */
    public static TradeRequest createSellRequest(String stockCode, Integer quantity, Integer price) {
        return TradeRequest.builder()
                .stockCode(stockCode)
                .tradeType("SELL")
                .quantity(quantity)
                .price(price)
                .orderType(price == null ? "MARKET" : "LIMIT")
                .build();
    }

    /**
     * AI 봇 매매 요청 생성 헬퍼 메서드
     * 
     * @param stockCode 종목 코드
     * @param tradeType 거래 타입
     * @param quantity 거래 수량
     * @param price 거래 가격
     * @return AI 봇 매매 요청 DTO
     */
    public static TradeRequest createAiBotRequest(String stockCode, String tradeType, 
                                                 Integer quantity, Integer price) {
        return TradeRequest.builder()
                .stockCode(stockCode)
                .tradeType(tradeType)
                .quantity(quantity)
                .price(price)
                .orderType("MARKET") // AI 봇은 시장가 주문만 사용
                .build();
=======
    @Schema(description = "거래 타입 (BUY/SELL)", example = "BUY", required = true)
    @NotBlank(message = "거래 타입은 필수입니다")
    private String tradeType;

    @Schema(description = "거래 수량", example = "10", required = true)
    @NotNull(message = "거래 수량은 필수입니다")
    @Positive(message = "거래 수량은 양수여야 합니다")
    private Integer quantity;

    @Schema(description = "거래 단가", example = "75000", required = true)
    @NotNull(message = "거래 단가는 필수입니다")
    @Positive(message = "거래 단가는 양수여야 합니다")
    private Integer price;

    @Schema(description = "계좌 ID", example = "1001")
    private Integer accountId; // 선택적 - 없으면 기본 계좌 사용
    
    /**
     * 매수 요청 유효성 검증
     * 지침서 명세: Validation/Exception 모든 입력/출력/관계/필수값/에러 꼼꼼히 처리
     */
    public boolean isValidBuyRequest() {
        return "BUY".equals(tradeType) && 
               stockCode != null && !stockCode.trim().isEmpty() &&
               quantity != null && quantity > 0 &&
               price != null && price > 0;
    }

    /**
     * 매도 요청 유효성 검증
     */
    public boolean isValidSellRequest() {
        return "SELL".equals(tradeType) && 
               stockCode != null && !stockCode.trim().isEmpty() &&
               quantity != null && quantity > 0 &&
               price != null && price > 0;
>>>>>>> origin/backend-dev
    }
}

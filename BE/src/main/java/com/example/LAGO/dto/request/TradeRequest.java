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
 * 매매 요청 DTO
 * 지침서 명세: 사용자 매매 요청 (POST /api/stocks/buy, /api/stocks/sell)
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
    }
}

package com.example.LAGO.controller;

import com.example.LAGO.dto.TradeRequest;
import com.example.LAGO.dto.TradeResponse;
import com.example.LAGO.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 주식 매매 관련 API 컨트롤러 (사용자용)
 * 지침서 명세: GET /api/stocks, POST /api/stocks/buy, POST /api/stocks/sell
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
@Tag(name = "주식 매매", description = "사용자 주식 매매 API (AI 봇과 분리)")
@Validated
public class StockController {

    private final TradeService tradeService;

    /**
     * 주식 매수
     * 지침서 명세: POST /api/stocks/buy
     * 
     * @param userId 사용자 ID (헤더 또는 인증에서 추출)
     * @param request 매수 요청
     * @return 매수 결과
     */
    @PostMapping("/buy")
    @Operation(
        summary = "주식 매수", 
        description = "사용자 계좌로 주식을 매수합니다. 계좌 잔액이 충분해야 합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "매수 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (잔액 부족, 유효하지 않은 종목 등)"),
        @ApiResponse(responseCode = "404", description = "사용자 또는 계좌를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<TradeResponse> buyStock(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("User-Id") // TODO: JWT 인증으로 변경 예정
            @NotNull(message = "사용자 ID는 필수입니다") 
            @Positive(message = "사용자 ID는 양수여야 합니다") 
            Integer userId,
            
            @Parameter(description = "매수 요청 정보", required = true)
            @RequestBody 
            @Valid 
            TradeRequest request
    ) {
        // 매수 타입 강제 설정
        request.setTradeType("BUY");
        
        TradeResponse response = tradeService.executeUserTrade(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 주식 매도
     * 지침서 명세: POST /api/stocks/sell
     * 
     * @param userId 사용자 ID
     * @param request 매도 요청
     * @return 매도 결과
     */
    @PostMapping("/sell")
    @Operation(
        summary = "주식 매도", 
        description = "사용자 보유 주식을 매도합니다. 충분한 보유 수량이 있어야 합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "매도 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (보유 수량 부족, 유효하지 않은 종목 등)"),
        @ApiResponse(responseCode = "404", description = "사용자 또는 계좌를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<TradeResponse> sellStock(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("User-Id") // TODO: JWT 인증으로 변경 예정
            @NotNull(message = "사용자 ID는 필수입니다") 
            @Positive(message = "사용자 ID는 양수여야 합니다") 
            Integer userId,
            
            @Parameter(description = "매도 요청 정보", required = true)
            @RequestBody 
            @Valid 
            TradeRequest request
    ) {
        // 매도 타입 강제 설정
        request.setTradeType("SELL");
        
        TradeResponse response = tradeService.executeUserTrade(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 매매 주문 (통합)
     * 매수/매도를 하나의 엔드포인트로 처리
     * 
     * @param userId 사용자 ID
     * @param request 매매 요청 (tradeType 포함)
     * @return 매매 결과
     */
    @PostMapping("/trade")
    @Operation(
        summary = "매매 주문 (통합)", 
        description = "매수/매도를 통합하여 처리합니다. tradeType에 따라 BUY/SELL을 구분합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "매매 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "사용자 또는 계좌를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<TradeResponse> executeTradeOrder(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("User-Id") // TODO: JWT 인증으로 변경 예정
            @NotNull(message = "사용자 ID는 필수입니다") 
            @Positive(message = "사용자 ID는 양수여야 합니다") 
            Integer userId,
            
            @Parameter(description = "매매 요청 정보", required = true)
            @RequestBody 
            @Valid 
            TradeRequest request
    ) {
        TradeResponse response = tradeService.executeUserTrade(userId, request);
        return ResponseEntity.ok(response);
    }
}

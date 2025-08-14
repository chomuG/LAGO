package com.example.LAGO.controller;

import com.example.LAGO.domain.TradeType;
import com.example.LAGO.dto.request.TradeRequest;
import com.example.LAGO.dto.response.TradeResponse;
import com.example.LAGO.service.TradeService;
import com.example.LAGO.service.MockTradingService;
import com.example.LAGO.service.OrderStreamProducer;
import com.example.LAGO.dto.request.MockTradeRequest;
import com.example.LAGO.dto.response.MockTradeResponse;
import com.example.LAGO.dto.OrderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class StockController {

    private final TradeService tradeService;
    private final MockTradingService mockTradingService;
    private final OrderStreamProducer orderStreamProducer;

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
            Long userId,
            
            @Parameter(description = "매수 요청 정보", required = true)
            @RequestBody 
            @Valid 
            TradeRequest request
    ) {
        // TradeRequest를 MockTradeRequest로 변환
        MockTradeRequest mockRequest = MockTradeRequest.builder()
                .stockCode(request.getStockCode())
                .tradeType(TradeType.BUY)
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .build();
        
        // MockTradingService를 사용하여 실제 계좌 잔액 업데이트
        MockTradeResponse mockResponse = mockTradingService.processBuyOrder(userId, mockRequest);
        
        // MockTradeResponse를 TradeResponse로 변환하여 반환
        TradeResponse response = TradeResponse.success(
                mockResponse.getTradeId() != null ? mockResponse.getTradeId() : 0L,
                userId,
                mockResponse.getStockCode(),
                mockResponse.getStockName(),
                TradeType.BUY,
                mockResponse.getQuantity(),
                mockResponse.getExecutedPrice(),
                mockResponse.getTotalAmount(),
                mockResponse.getCommission(),
                0, // tax
                mockResponse.getRemainingBalance(),
                mockResponse.getMessage()
        );
        
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
            Long userId,
            
            @Parameter(description = "매도 요청 정보", required = true)
            @RequestBody 
            @Valid 
            TradeRequest request
    ) {
        // TradeRequest를 MockTradeRequest로 변환
        MockTradeRequest mockRequest = MockTradeRequest.builder()
                .stockCode(request.getStockCode())
                .tradeType(TradeType.SELL)
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .build();
        
        // MockTradingService를 사용하여 실제 계좌 잔액 업데이트
        MockTradeResponse mockResponse = mockTradingService.processSellOrder(userId, mockRequest);
        
        // MockTradeResponse를 TradeResponse로 변환하여 반환
        TradeResponse response = TradeResponse.success(
                mockResponse.getTradeId() != null ? mockResponse.getTradeId() : 0L,
                userId,
                mockResponse.getStockCode(),
                mockResponse.getStockName(),
                TradeType.SELL,
                mockResponse.getQuantity(),
                mockResponse.getExecutedPrice(),
                mockResponse.getTotalAmount(),
                mockResponse.getCommission(),
                0, // tax
                mockResponse.getRemainingBalance(),
                mockResponse.getMessage()
        );
        
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
            Long userId,
            
            @Parameter(description = "매매 요청 정보", required = true)
            @RequestBody 
            @Valid 
            TradeRequest request
    ) {
        // TradeRequest를 MockTradeRequest로 변환
        MockTradeRequest mockRequest = MockTradeRequest.builder()
                .stockCode(request.getStockCode())
                .tradeType(request.getTradeType())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .build();
        
        // 거래 타입에 따라 적절한 서비스 메서드 호출
        MockTradeResponse mockResponse;
        if (TradeType.BUY.equals(request.getTradeType())) {
            mockResponse = mockTradingService.processBuyOrder(userId, mockRequest);
        } else if (TradeType.SELL.equals(request.getTradeType())) {
            mockResponse = mockTradingService.processSellOrder(userId, mockRequest);
        } else {
            throw new IllegalArgumentException("지원되지 않는 거래 타입: " + request.getTradeType());
        }
        
        // MockTradeResponse를 TradeResponse로 변환하여 반환
        TradeResponse response = TradeResponse.success(
                mockResponse.getTradeId() != null ? mockResponse.getTradeId() : 0L,
                userId,
                mockResponse.getStockCode(),
                mockResponse.getStockName(),
                request.getTradeType(),
                mockResponse.getQuantity(),
                mockResponse.getExecutedPrice(),
                mockResponse.getTotalAmount(),
                mockResponse.getCommission(),
                0, // tax
                mockResponse.getRemainingBalance(),
                mockResponse.getMessage()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * 비동기 매매 주문 (Redis Stream 기반)
     * 즉시 주문 접수 응답 후 백그라운드에서 처리
     * 
     * @param userId 사용자 ID
     * @param request 매매 요청
     * @return 주문 접수 응답
     */
    @PostMapping("/order")
    @Operation(
        summary = "비동기 매매 주문", 
        description = "Redis Stream을 통한 비동기 매매 처리. 즉시 주문 접수 응답 후 백그라운드에서 실제 매매 수행"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "주문 접수 완료 (비동기 처리)"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<?> submitAsyncOrder(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("User-Id")
            @NotNull(message = "사용자 ID는 필수입니다") 
            @Positive(message = "사용자 ID는 양수여야 합니다") 
            Long userId,
            
            @Parameter(description = "매매 요청 정보", required = true)
            @RequestBody 
            @Valid 
            TradeRequest request
    ) {
        try {
            // TradeRequest를 OrderDto로 변환
            OrderDto orderDto = OrderDto.builder()
                    .userId(userId)
                    .stockCode(request.getStockCode())
                    .tradeType(request.getTradeType())
                    .quantity(request.getQuantity())
                    .price(request.getPrice())
                    .accountId(request.getAccountId() != null ? request.getAccountId().longValue() : null)
                    .status(OrderDto.OrderStatus.PENDING)
                    .priority(1) // 일반 주문 우선순위
                    .build();
            
            // Redis Stream에 주문 발행
            String orderId = orderStreamProducer.publishOrder(orderDto);
            
            // 즉시 주문 접수 응답
            return ResponseEntity.accepted()
                    .body(java.util.Map.of(
                        "success", true,
                        "orderId", orderId,
                        "userId", userId,
                        "stockCode", request.getStockCode(),
                        "tradeType", request.getTradeType().name(),
                        "quantity", request.getQuantity(),
                        "status", "ACCEPTED",
                        "message", "주문이 접수되었습니다. 백그라운드에서 처리 중입니다.",
                        "submittedAt", java.time.LocalDateTime.now()
                    ));
            
        } catch (Exception e) {
            log.error("비동기 주문 접수 실패: userId={}, stockCode={}, error={}", 
                    userId, request.getStockCode(), e.getMessage(), e);
            
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of(
                        "success", false,
                        "error", "ORDER_SUBMISSION_FAILED",
                        "message", "주문 접수에 실패했습니다: " + e.getMessage(),
                        "userId", userId,
                        "stockCode", request.getStockCode()
                    ));
        }
    }

    /**
     * 주문 상태 조회
     * 
     * @param orderId 주문 ID (Redis Stream Record ID)
     * @return 주문 상태 정보
     */
    @GetMapping("/order/{orderId}/status")
    @Operation(
        summary = "주문 상태 조회", 
        description = "비동기 주문의 처리 상태를 조회합니다"
    )
    public ResponseEntity<?> getOrderStatus(
            @Parameter(description = "주문 ID", required = true)
            @PathVariable String orderId
    ) {
        try {
            // TODO: Redis에서 주문 상태 조회 로직 구현
            // 현재는 기본 응답 반환
            return ResponseEntity.ok(java.util.Map.of(
                "orderId", orderId,
                "status", "PROCESSING",
                "message", "주문 처리 중입니다",
                "checkedAt", java.time.LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of(
                        "success", false,
                        "error", "ORDER_STATUS_CHECK_FAILED",
                        "message", "주문 상태 조회에 실패했습니다: " + e.getMessage(),
                        "orderId", orderId
                    ));
        }
    }

    /**
     * Stream 상태 확인 (관리자용)
     * 
     * @return Stream 상태 정보
     */
    @GetMapping("/stream/status")
    @Operation(
        summary = "주문 처리 스트림 상태", 
        description = "Redis Stream 기반 주문 처리 시스템의 상태를 확인합니다"
    )
    public ResponseEntity<?> getStreamStatus() {
        try {
            String streamInfo = orderStreamProducer.getStreamInfo();
            boolean streamAvailable = orderStreamProducer.isStreamAvailable();
            
            return ResponseEntity.ok(java.util.Map.of(
                "streamAvailable", streamAvailable,
                "streamInfo", streamInfo,
                "checkedAt", java.time.LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Map.of(
                "streamAvailable", false,
                "error", e.getMessage(),
                "checkedAt", java.time.LocalDateTime.now()
            ));
        }
    }
}

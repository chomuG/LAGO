package com.example.LAGO.controller;

import com.example.LAGO.domain.TradeType;
import com.example.LAGO.dto.request.TradeRequest;
import com.example.LAGO.service.OrderStreamProducer;
import com.example.LAGO.service.MockTradingService;
import com.example.LAGO.dto.OrderDto;
import com.example.LAGO.dto.request.MockTradeRequest;
import com.example.LAGO.dto.response.MockTradeResponse;
import com.example.LAGO.domain.User;
import com.example.LAGO.domain.Account;
import com.example.LAGO.domain.StockInfo;
import com.example.LAGO.repository.UserRepository;
import com.example.LAGO.repository.AccountRepository;
import com.example.LAGO.repository.StockInfoRepository;
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

    private final OrderStreamProducer orderStreamProducer;
    private final MockTradingService mockTradingService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final StockInfoRepository stockInfoRepository;




    /**
     * 실시간 가격 매매 주문 (Redis Stream 기반)
     * 웹소켓으로 받은 실시간 가격으로 즉시 매매 처리
     * 
     * @param userId 사용자 ID
     * @param request 매매 요청 (price=null이면 시장가, 값이 있으면 지정가)
     * @return 주문 접수 응답
     */
    @PostMapping("/order")
    @Operation(
        summary = "실시간 가격 매매 주문", 
        description = "웹소켓 실시간 가격을 사용한 매매 처리. price=null이면 시장가(실시간 가격), 값이 있으면 지정가 주문"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "주문 접수 완료 (비동기 처리)"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<?> submitAsyncOrder(
            @Parameter(description = "매매 요청 정보", required = true)
            @RequestBody 
            @Valid 
            TradeRequest request
    ) {
        try {
            // 1. 사용자 존재 여부 검증
            User user = userRepository.findById(request.getUserId())
                .orElse(null);
            if (user == null) {
                log.warn("존재하지 않는 사용자 주문 시도: userId={}", request.getUserId());
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of(
                        "success", false,
                        "error", "USER_NOT_FOUND",
                        "message", "존재하지 않는 사용자입니다.",
                        "userId", request.getUserId()
                    ));
            }
            
            // 2. 계좌 타입 유효성 검증 (0, 1, 2만 허용)
            Integer accountType = request.getAccountType();
            if (accountType == null || (accountType != 0 && accountType != 1 && accountType != 2)) {
                log.warn("잘못된 계좌 타입: userId={}, accountType={}", request.getUserId(), accountType);
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of(
                        "success", false,
                        "error", "INVALID_ACCOUNT_TYPE",
                        "message", "잘못된 계좌 타입입니다. (0=실시간모의투자, 1=역사챌린지, 2=자동매매봇)",
                        "userId", request.getUserId(),
                        "accountType", accountType != null ? accountType : "null"
                    ));
            }
            
            // 3. 계좌 존재 여부 검증
            Account account = accountRepository.findByUserIdAndType(request.getUserId(), accountType)
                .orElse(null);
            if (account == null) {
                String accountTypeName = getAccountTypeName(accountType);
                log.warn("계좌를 찾을 수 없음: userId={}, accountType={} ({})", 
                        request.getUserId(), accountType, accountTypeName);
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of(
                        "success", false,
                        "error", "ACCOUNT_NOT_FOUND",
                        "message", String.format("계좌를 찾을 수 없습니다. (%s)", accountTypeName),
                        "userId", request.getUserId(),
                        "accountType", accountType,
                        "accountTypeName", accountTypeName
                    ));
            }
            
            // 4. 종목 코드 유효성 검증
            StockInfo stockInfo = stockInfoRepository.findByCode(request.getStockCode())
                .orElse(null);
            if (stockInfo == null) {
                log.warn("존재하지 않는 종목 코드: userId={}, stockCode={}", 
                        request.getUserId(), request.getStockCode());
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of(
                        "success", false,
                        "error", "STOCK_NOT_FOUND",
                        "message", "존재하지 않는 종목 코드입니다.",
                        "userId", request.getUserId(),
                        "stockCode", request.getStockCode()
                    ));
            }
            
            // 5. 매수 주문인 경우 잔고 충분성 검증
            if (TradeType.BUY.equals(request.getTradeType())) {
                Integer price = request.getPrice();
                Integer quantity = request.getQuantity();
                Integer requiredAmount = price * quantity;
                
                // 수수료 추가 (0.25%)
                Integer commission = (int) Math.round(requiredAmount * 0.0025);
                Integer totalRequired = requiredAmount + commission;
                
                if (account.getBalance() < totalRequired) {
                    log.warn("잔고 부족: userId={}, 필요금액={}, 보유잔고={}", 
                            request.getUserId(), totalRequired, account.getBalance());
                    return ResponseEntity.badRequest()
                        .body(java.util.Map.of(
                            "success", false,
                            "error", "INSUFFICIENT_BALANCE",
                            "message", String.format("잔고가 부족합니다. 필요: %,d원, 보유: %,d원", 
                                    totalRequired, account.getBalance()),
                            "userId", request.getUserId(),
                            "requiredAmount", totalRequired,
                            "currentBalance", account.getBalance()
                        ));
                }
            }
            
            // 6. 모든 검증 통과 시 주문 발행
            // TradeRequest를 OrderDto로 변환
            OrderDto orderDto = OrderDto.builder()
                    .userId(request.getUserId())
                    .stockCode(request.getStockCode())
                    .tradeType(request.getTradeType())
                    .quantity(request.getQuantity())
                    .price(request.getPrice())
                    .accountId(request.getAccountType().longValue())
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
                        "userId", request.getUserId(),
                        "stockCode", request.getStockCode(),
                        "tradeType", request.getTradeType().name(),
                        "quantity", request.getQuantity(),
                        "status", "ACCEPTED",
                        "message", "주문이 접수되었습니다. 백그라운드에서 처리 중입니다.",
                        "submittedAt", java.time.LocalDateTime.now()
                    ));
            
        } catch (Exception e) {
            log.error("비동기 주문 접수 실패: userId={}, stockCode={}, error={}", 
                    request.getUserId(), request.getStockCode(), e.getMessage(), e);
            
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of(
                        "success", false,
                        "error", "ORDER_SUBMISSION_FAILED",
                        "message", "주문 접수에 실패했습니다: " + e.getMessage(),
                        "userId", request.getUserId(),
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

    /**
     * 계좌 타입명 반환
     */
    private String getAccountTypeName(Integer accountType) {
        return switch (accountType) {
            case 0 -> "실시간 모의투자";
            case 1 -> "역사챌린지";
            case 2 -> "자동매매봇";
            default -> "알 수 없는 계좌";
        };
    }
}

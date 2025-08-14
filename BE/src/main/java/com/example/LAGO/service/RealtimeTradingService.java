package com.example.LAGO.service;

import com.example.LAGO.domain.TradeType;
import com.example.LAGO.realtime.dto.TickData;
import com.example.LAGO.dto.OrderDto;
import com.example.LAGO.dto.request.MockTradeRequest;
import com.example.LAGO.dto.response.MockTradeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 실시간 매매 처리 서비스
 * WebSocket에서 실시간 가격 데이터를 받으면 즉시 대기 중인 주문을 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RealtimeTradingService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final MockTradingService mockTradingService;
    private final ObjectMapper objectMapper;
    
    // Redis에서 대기 중인 주문을 저장하는 키 패턴
    private static final String PENDING_ORDERS_KEY = "pending_orders:";
    
    /**
     * 실시간 가격 데이터를 받았을 때 해당 종목의 대기 중인 주문들을 처리
     * 
     * @param tickData 실시간 가격 데이터
     */
    public void processRealtimeOrders(TickData tickData) {
        String stockCode = tickData.getCode();
        Integer currentPrice = tickData.getClosePrice();
        
        if (stockCode == null || currentPrice == null) {
            log.warn("Invalid tick data for realtime trading: stockCode={}, price={}", stockCode, currentPrice);
            return;
        }
        
        try {
            // 해당 종목의 대기 중인 주문 조회
            String pendingOrdersKey = PENDING_ORDERS_KEY + stockCode;
            Set<String> pendingOrders = redisTemplate.opsForSet().members(pendingOrdersKey);
            
            if (pendingOrders == null || pendingOrders.isEmpty()) {
                return; // 대기 중인 주문이 없음
            }
            
            log.info("Processing {} pending orders for stock {} at price {}", 
                    pendingOrders.size(), stockCode, currentPrice);
            
            // 각 대기 중인 주문을 실시간 가격으로 처리
            for (String orderData : pendingOrders) {
                processRealtimeOrder(orderData, currentPrice);
                
                // 처리 완료된 주문을 Redis에서 제거
                redisTemplate.opsForSet().remove(pendingOrdersKey, orderData);
            }
            
        } catch (Exception e) {
            log.error("Error processing realtime orders for stock {}: {}", stockCode, e.getMessage(), e);
        }
    }
    
    /**
     * 개별 주문을 실시간 가격으로 처리
     * 
     * @param orderData JSON 형태의 주문 데이터
     * @param realtimePrice 실시간 가격
     */
    private void processRealtimeOrder(String orderData, Integer realtimePrice) {
        try {
            // OrderDto로 파싱
            OrderDto order = parseOrderData(orderData);
            
            if (order == null) {
                log.warn("Failed to parse order data: {}", orderData);
                return;
            }
            
            // 실시간 가격으로 주문 실행
            log.info("Executing realtime order: userId={}, stockCode={}, tradeType={}, quantity={}, realtimePrice={}", 
                    order.getUserId(), order.getStockCode(), order.getTradeType(), 
                    order.getQuantity(), realtimePrice);
            
            // MockTradeRequest 생성 (실시간 가격으로)
            MockTradeRequest mockRequest = MockTradeRequest.builder()
                    .stockCode(order.getStockCode())
                    .tradeType(order.getTradeType())
                    .quantity(order.getQuantity())
                    .price(realtimePrice) // 실시간 가격 사용
                    .build();
            
            // MockTradingService를 통한 실제 매매 처리 (올바른 계좌 타입으로)
            Integer accountType = order.getAccountId() != null ? order.getAccountId().intValue() : null;
            MockTradeResponse response;
            if (TradeType.BUY.equals(order.getTradeType())) {
                response = mockTradingService.processBuyOrder(order.getUserId(), mockRequest, accountType);
            } else {
                response = mockTradingService.processSellOrder(order.getUserId(), mockRequest, accountType);
            }
            
            if (Boolean.TRUE.equals(response.getSuccess())) {
                log.info("✅ Realtime order executed successfully: tradeId={}, userId={}, stockCode={}, price={}", 
                        response.getTradeId(), order.getUserId(), order.getStockCode(), realtimePrice);
            } else {
                log.warn("❌ Realtime order failed: userId={}, stockCode={}, error={}", 
                        order.getUserId(), order.getStockCode(), response.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("Error processing individual order: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 새로운 주문을 대기 큐에 추가
     * 
     * @param order 주문 정보
     */
    public void addPendingOrder(OrderDto order) {
        try {
            String stockCode = order.getStockCode();
            String pendingOrdersKey = PENDING_ORDERS_KEY + stockCode;
            String orderData = serializeOrderData(order);
            
            redisTemplate.opsForSet().add(pendingOrdersKey, orderData);
            
            log.info("Added pending order to queue: userId={}, stockCode={}, tradeType={}", 
                    order.getUserId(), order.getStockCode(), order.getTradeType());
            
        } catch (Exception e) {
            log.error("Error adding pending order: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 주문 데이터를 JSON 문자열로 직렬화
     */
    private String serializeOrderData(OrderDto order) {
        try {
            return objectMapper.writeValueAsString(order);
        } catch (Exception e) {
            log.error("Error serializing order data: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * JSON 문자열을 OrderDto로 파싱
     */
    private OrderDto parseOrderData(String orderData) {
        try {
            return objectMapper.readValue(orderData, OrderDto.class);
        } catch (Exception e) {
            log.error("Error parsing order data: {}", orderData, e);
            return null;
        }
    }
    
    // ==================== 테스트용 메서드들 ====================
    
    /**
     * 특정 종목의 대기 중인 주문 개수 조회 (테스트용)
     */
    public int getPendingOrdersCount(String stockCode) {
        try {
            String pendingOrdersKey = PENDING_ORDERS_KEY + stockCode;
            Long count = redisTemplate.opsForSet().size(pendingOrdersKey);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.error("Error getting pending orders count for {}: {}", stockCode, e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 모든 대기 주문 초기화 (테스트용)
     */
    public int clearAllPendingOrders() {
        try {
            int clearedCount = 0;
            
            // pending_orders:* 패턴의 모든 키 조회
            Set<String> pendingKeys = redisTemplate.keys(PENDING_ORDERS_KEY + "*");
            
            if (pendingKeys != null && !pendingKeys.isEmpty()) {
                for (String key : pendingKeys) {
                    Long deleted = redisTemplate.delete(key) ? 1L : 0L;
                    clearedCount += deleted.intValue();
                }
            }
            
            log.info("Cleared {} pending order keys", clearedCount);
            return clearedCount;
            
        } catch (Exception e) {
            log.error("Error clearing all pending orders: {}", e.getMessage(), e);
            return 0;
        }
    }
}
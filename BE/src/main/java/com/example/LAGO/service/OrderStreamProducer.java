package com.example.LAGO.service;

import com.example.LAGO.dto.OrderDto;
import com.example.LAGO.dto.OrderResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Redis Stream 주문 발행 서비스
 * 매수/매도 주문을 Redis Stream에 발행하여 비동기 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStreamProducer {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${spring.redis.stream.key:orders-stream}")
    private String orderStreamKey;
    
    @Value("${spring.redis.stream.result-key:order-results-stream}")
    private String resultStreamKey;
    
    /**
     * 주문을 Redis Stream에 발행
     * 
     * @param orderDto 주문 정보
     * @return 발행된 주문의 Record ID
     */
    public String publishOrder(OrderDto orderDto) {
        try {
            log.info("주문 발행 시작: userId={}, stockCode={}, tradeType={}, quantity={}", 
                    orderDto.getUserId(), orderDto.getStockCode(), 
                    orderDto.getTradeType(), orderDto.getQuantity());
            
            // 주문 유효성 검증
            if (!orderDto.isValid()) {
                throw new IllegalArgumentException("Invalid order data: " + orderDto);
            }
            
            // 주문 시간 설정
            if (orderDto.getOrderTime() == null) {
                orderDto.setOrderTime(LocalDateTime.now());
            }
            
            // 주문 상태 설정
            if (orderDto.getStatus() == null) {
                orderDto.setStatus(OrderDto.OrderStatus.PENDING);
            }
            
            // 우선순위 기본값 설정
            if (orderDto.getPriority() == null) {
                orderDto.setPriority(1); // 일반 주문 우선순위
            }
            
            // Redis Stream Record 생성
            StringRecord record = StreamRecords.string(orderDto.toStreamFields())
                    .withStreamKey(orderStreamKey);
            
            // Stream에 발행
            RecordId recordId = redisTemplate.opsForStream().add(record);
            
            log.info("주문 발행 완료: recordId={}, userId={}, stockCode={}", 
                    recordId.getValue(), orderDto.getUserId(), orderDto.getStockCode());
            
            return recordId.getValue();
            
        } catch (Exception e) {
            log.error("주문 발행 실패: userId={}, stockCode={}, error={}", 
                    orderDto.getUserId(), orderDto.getStockCode(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish order: " + e.getMessage(), e);
        }
    }
    
    /**
     * 주문을 비동기로 발행 (Future 반환)
     * 
     * @param orderDto 주문 정보
     * @return 발행 결과 Future
     */
    public CompletableFuture<String> publishOrderAsync(OrderDto orderDto) {
        return CompletableFuture.supplyAsync(() -> publishOrder(orderDto));
    }
    
    /**
     * 주문 처리 결과를 결과 Stream에 발행
     * 
     * @param resultDto 처리 결과
     * @return 발행된 결과의 Record ID
     */
    public String publishOrderResult(OrderResultDto resultDto) {
        try {
            log.debug("주문 결과 발행: orderId={}, success={}, userId={}", 
                    resultDto.getOrderId(), resultDto.isSuccess(), resultDto.getUserId());
            
            // Redis Stream Record 생성
            StringRecord record = StreamRecords.string(resultDto.toStreamFields())
                    .withStreamKey(resultStreamKey);
            
            // Stream에 발행
            RecordId recordId = redisTemplate.opsForStream().add(record);
            
            log.debug("주문 결과 발행 완료: recordId={}, orderId={}", 
                    recordId.getValue(), resultDto.getOrderId());
            
            return recordId.getValue();
            
        } catch (Exception e) {
            log.error("주문 결과 발행 실패: orderId={}, error={}", 
                    resultDto.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish order result: " + e.getMessage(), e);
        }
    }
    
    /**
     * 주문 스트림 상태 조회
     * 
     * @return 스트림 정보
     */
    public String getStreamInfo() {
        try {
            Long length = redisTemplate.opsForStream().size(orderStreamKey);
            return String.format("Order Stream '%s' length: %d", orderStreamKey, length);
        } catch (Exception e) {
            log.warn("스트림 정보 조회 실패: {}", e.getMessage());
            return "Stream info unavailable: " + e.getMessage();
        }
    }
    
    /**
     * 주문 발행 상태 확인 (스트림 존재 여부)
     * 
     * @return 스트림 사용 가능 여부
     */
    public boolean isStreamAvailable() {
        try {
            return redisTemplate.hasKey(orderStreamKey);
        } catch (Exception e) {
            log.error("스트림 상태 확인 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 긴급 주문 발행 (높은 우선순위)
     * 
     * @param orderDto 주문 정보
     * @return 발행된 주문의 Record ID
     */
    public String publishUrgentOrder(OrderDto orderDto) {
        orderDto.setPriority(10); // 높은 우선순위 설정
        orderDto.setStatus(OrderDto.OrderStatus.PENDING);
        
        log.info("긴급 주문 발행: userId={}, stockCode={}, priority={}", 
                orderDto.getUserId(), orderDto.getStockCode(), orderDto.getPriority());
        
        return publishOrder(orderDto);
    }
}
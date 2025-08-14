package com.example.LAGO.service;

import com.example.LAGO.dto.OrderDto;
import com.example.LAGO.dto.OrderResultDto;
import com.example.LAGO.dto.request.MockTradeRequest;
import com.example.LAGO.dto.response.MockTradeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.LocalDateTime;

/**
 * Redis Stream 주문 소비 서비스
 * 주문 스트림에서 주문을 소비하여 실제 매매 처리 수행
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStreamConsumer {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final MockTradingService mockTradingService;
    private final OrderStreamProducer orderStreamProducer;
    
    @Value("${spring.redis.stream.key:orders-stream}")
    private String orderStreamKey;
    
    @Value("${redis.stream.consumer-group:lago_order_group}")
    private String consumerGroup;
    
    @Value("${redis.stream.consumer-name:lago_order_consumer}")
    private String consumerName;
    
    @Value("${redis.stream.read-timeout:2000}")
    private long readTimeout;
    
    @Value("${redis.stream.batch-size:5}")
    private int batchSize;
    
    private ExecutorService executorService;
    private Future<?> consumerTask;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    // 메트릭 정보
    private volatile long totalOrdersProcessed = 0;
    private volatile long totalOrdersSucceeded = 0;
    private volatile long totalOrdersFailed = 0;
    private volatile LocalDateTime lastProcessTime = null;
    
    @PostConstruct
    public void initialize() {
        log.info("🚀 OrderStreamConsumer 초기화 중...");
        log.info("Stream Key: {}, Consumer Group: {}, Consumer Name: {}", 
                orderStreamKey, consumerGroup, consumerName);
        
        // Consumer Group 생성
        createConsumerGroupIfNotExists();
        
        // Virtual Thread 실행기 생성 (Java 21)
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        
        // Consumer 자동 시작
        startConsumer();
    }
    
    /**
     * Consumer Group 생성 (존재하지 않는 경우에만)
     */
    private void createConsumerGroupIfNotExists() {
        try {
            // Stream이 없으면 먼저 생성
            if (!redisTemplate.hasKey(orderStreamKey)) {
                log.info("📝 주문 Stream '{}' 생성 중...", orderStreamKey);
                // 더미 데이터로 Stream 생성
                redisTemplate.opsForStream().add(orderStreamKey, 
                    java.util.Map.of("init", "order_stream_initialized"));
                // 더미 데이터 제거 (Stream은 유지됨)
                redisTemplate.opsForStream().trim(orderStreamKey, 0);
            }
            
            // Consumer Group 생성
            redisTemplate.opsForStream().createGroup(orderStreamKey, ReadOffset.from("0"), consumerGroup);
            log.info("✅ 주문 Consumer Group '{}' 생성 완료", consumerGroup);
            
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                log.info("✅ 주문 Consumer Group '{}' 이미 존재", consumerGroup);
            } else {
                log.error("❌ 주문 Consumer Group 생성 실패: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Consumer 시작
     */
    public void startConsumer() {
        if (running.compareAndSet(false, true)) {
            log.info("▶️ 주문 Stream Consumer 시작");
            consumerTask = executorService.submit(this::consumeOrders);
        } else {
            log.warn("⚠️ 주문 Consumer가 이미 실행 중입니다");
        }
    }
    
    /**
     * Consumer 중지
     */
    public void stopConsumer() {
        if (running.compareAndSet(true, false)) {
            log.info("⏹️ 주문 Stream Consumer 중지");
            if (consumerTask != null) {
                consumerTask.cancel(true);
            }
        }
    }
    
    /**
     * 주문 스트림 소비 메인 로직
     */
    private void consumeOrders() {
        log.info("🔄 주문 Stream 소비 시작 - Key: {}", orderStreamKey);
        
        while (running.get()) {
            try {
                // Consumer Group에서 주문 읽기
                StreamReadOptions readOptions = StreamReadOptions.empty()
                        .count(batchSize)
                        .block(Duration.ofMillis(readTimeout));
                
                Consumer consumer = Consumer.from(consumerGroup, consumerName);
                StreamOffset<String> streamOffset = StreamOffset.create(orderStreamKey, ReadOffset.lastConsumed());
                
                // Stream에서 주문 읽기
                @SuppressWarnings("unchecked")
                List<MapRecord<String, Object, Object>> records = 
                    redisTemplate.opsForStream().read(consumer, readOptions, streamOffset);
                
                if (records != null && !records.isEmpty()) {
                    log.debug("📥 {} 개의 주문 수신", records.size());
                    
                    // 각 주문 처리
                    for (MapRecord<String, Object, Object> record : records) {
                        processOrderRecord(record);
                    }
                }
                
            } catch (Exception e) {
                if (running.get()) {
                    log.error("❌ 주문 Stream 소비 중 오류 발생: {}", e.getMessage(), e);
                    
                    // 오류 발생 시 잠시 대기
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        log.info("🔚 주문 Stream 소비 종료");
    }
    
    /**
     * 개별 주문 Record 처리
     */
    private void processOrderRecord(MapRecord<String, Object, Object> record) {
        String recordId = record.getId().getValue();
        OrderResultDto result = null;
        
        try {
            log.debug("📨 주문 Record ID: {}, Fields: {}", recordId, record.getValue());
            
            // Stream 데이터를 OrderDto로 변환
            OrderDto orderDto = OrderDto.fromStreamFields(recordId, record.getValue());
            
            if (!orderDto.isValid()) {
                throw new IllegalArgumentException("Invalid order data: " + orderDto);
            }
            
            log.info("🔄 주문 처리 시작: orderId={}, userId={}, stockCode={}, tradeType={}", 
                    orderDto.getOrderId(), orderDto.getUserId(), 
                    orderDto.getStockCode(), orderDto.getTradeType());
            
            // 주문을 MockTradeRequest로 변환
            MockTradeRequest mockRequest = MockTradeRequest.builder()
                    .stockCode(orderDto.getStockCode())
                    .tradeType(orderDto.getTradeType())
                    .quantity(orderDto.getQuantity())
                    .price(orderDto.getPrice())
                    .build();
            
            // 실제 매매 처리
            MockTradeResponse mockResponse;
            switch (orderDto.getTradeType()) {
                case BUY:
                    mockResponse = mockTradingService.processBuyOrder(orderDto.getUserId(), mockRequest);
                    break;
                case SELL:
                    mockResponse = mockTradingService.processSellOrder(orderDto.getUserId(), mockRequest);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported trade type: " + orderDto.getTradeType());
            }
            
            // 성공 결과 생성
            if (Boolean.TRUE.equals(mockResponse.getSuccess())) {
                result = OrderResultDto.success(
                    recordId,
                    orderDto.getUserId(),
                    orderDto.getStockCode(),
                    mockResponse.getStockName(),
                    orderDto.getTradeType(),
                    mockResponse.getQuantity(),
                    mockResponse.getExecutedPrice(),
                    mockResponse.getTotalAmount(),
                    mockResponse.getCommission(),
                    0, // tax
                    mockResponse.getRemainingBalance(),
                    mockResponse.getTradeId(),
                    mockResponse.getMessage()
                );
                
                totalOrdersSucceeded++;
                log.info("✅ 주문 처리 성공: orderId={}, tradeId={}", recordId, mockResponse.getTradeId());
                
            } else {
                result = OrderResultDto.failure(
                    recordId,
                    orderDto.getUserId(),
                    orderDto.getStockCode(),
                    "TRADE_FAILED",
                    mockResponse.getErrorMessage() != null ? mockResponse.getErrorMessage() : mockResponse.getMessage()
                );
                
                totalOrdersFailed++;
                log.warn("⚠️ 주문 처리 실패: orderId={}, reason={}", recordId, 
                    mockResponse.getErrorMessage() != null ? mockResponse.getErrorMessage() : mockResponse.getMessage());
            }
            
        } catch (Exception e) {
            totalOrdersFailed++;
            log.error("❌ 주문 처리 중 오류 발생 - Record ID: {}, Error: {}", recordId, e.getMessage(), e);
            
            // OrderDto에서 기본 정보 추출 시도
            Long userId = null;
            String stockCode = null;
            try {
                var fields = record.getValue();
                userId = Long.valueOf(fields.get("userId").toString());
                stockCode = fields.get("stockCode").toString();
            } catch (Exception ignored) {}
            
            result = OrderResultDto.failure(
                recordId, userId, stockCode,
                "PROCESSING_ERROR", e.getMessage()
            );
            
        } finally {
            try {
                // 처리 결과 발행
                if (result != null) {
                    orderStreamProducer.publishOrderResult(result);
                }
                
                // 메시지 처리 완료 확인응답
                redisTemplate.opsForStream().acknowledge(orderStreamKey, consumerGroup, record.getId());
                
                totalOrdersProcessed++;
                lastProcessTime = LocalDateTime.now();
                
            } catch (Exception ackError) {
                log.error("❌ ACK 또는 결과 발행 실패 - Record ID: {}, Error: {}", 
                        recordId, ackError.getMessage());
            }
        }
    }
    
    /**
     * Consumer 상태 정보 반환
     */
    public String getConsumerStatus() {
        return String.format(
            "Order Consumer Status: %s | Processed: %d | Succeeded: %d | Failed: %d | Last Process: %s",
            running.get() ? "RUNNING" : "STOPPED",
            totalOrdersProcessed,
            totalOrdersSucceeded, 
            totalOrdersFailed,
            lastProcessTime != null ? lastProcessTime.toString() : "Never"
        );
    }
    
    /**
     * Consumer 메트릭 주기적 로깅
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 30000) // 30초마다
    public void logConsumerMetrics() {
        if (running.get() && totalOrdersProcessed > 0) {
            log.info("📊 {}", getConsumerStatus());
        }
    }
    
    @PreDestroy
    public void cleanup() {
        log.info("🧹 OrderStreamConsumer 정리 중...");
        log.info("📊 최종 통계 - {}", getConsumerStatus());
        
        stopConsumer();
        
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        log.info("✅ OrderStreamConsumer 정리 완료");
    }
}
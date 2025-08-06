package com.example.LAGO.service;

import com.example.LAGO.domain.*;
import com.example.LAGO.dto.request.MockTradeRequestDto;
import com.example.LAGO.dto.TradeRequest;
import com.example.LAGO.dto.response.MockTradeResponseDto;
import com.example.LAGO.dto.TradeResponse;
import com.example.LAGO.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 거래 처리 서비스
 * 
 * 실제 거래 및 모의 거래 처리를 담당하는 서비스
 * 
 * @author 라고할때 팀
 * @version 1.0
 * @since 2025-01-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TradeService {
    
    private final MockTradeRepository mockTradeRepository;
    private final StockInfoRepository stockInfoRepository;
    private final UserRepository userRepository;
    
    /**
     * 모의 거래 처리 (간소화 버전)
     */
    public MockTradeResponseDto processMockTrade(MockTradeRequestDto request) {
        try {
            log.info("모의 거래 처리 시작: stockCode={}, tradeType={}", 
                    request.getStockCode(), request.getTradeType());
            
            // 기본 검증
            if (request.getStockCode() == null || request.getTradeType() == null) {
                return MockTradeResponseDto.failure("ERROR", "필수 파라미터가 누락되었습니다.");
            }
            
            // 거래 기록 생성 (간소화)
            MockTrade mockTrade = MockTrade.builder()
                    .stockCode(request.getStockCode())
                    .tradeType(request.getTradeType())
                    .quantity(request.getQuantity())
                    .price(1000) // 임시 가격
                    .totalAmount(1000 * request.getQuantity())
                    .tradeTime(LocalDateTime.now())
                    .status("COMPLETED")
                    .build();
            
            // 거래 기록 저장
            mockTradeRepository.save(mockTrade);
            
            log.info("모의 거래 처리 완료: tradeId={}", mockTrade.getTradeId());
            
            return MockTradeResponseDto.success(
                    mockTrade.getTradeId(), // Long 타입 그대로 사용
                    mockTrade.getStockCode(),
                    mockTrade.getStockCode(), // stockName 대신 임시로 stockCode 사용
                    mockTrade.getQuantity(),
                    mockTrade.getPrice(),
                    mockTrade.getTotalAmount(),
                    0, // commission
                    0, // remainingBalance (임시)
                    mockTrade.getTradeType()
            );
            
        } catch (Exception e) {
            log.error("모의 거래 처리 오류: {}", e.getMessage(), e);
            return MockTradeResponseDto.failure(
                    "ERROR",
                    "거래 처리 실패: " + e.getMessage()
            );
        }
    }
    
    /**
     * 거래 검증 (간소화)
     */
    public boolean validateTrade(MockTradeRequestDto request) {
        try {
            return request.getStockCode() != null && 
                   request.getTradeType() != null && 
                   request.getQuantity() != null && 
                   request.getQuantity() > 0;
        } catch (Exception e) {
            log.error("거래 검증 오류: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 사용자 거래 실행 (StockController에서 호출)
     * 지침서 명세: API 명세서 기준, 파라미터/반환 구조 임의 변경 금지
     */
    public TradeResponse executeUserTrade(Integer userId, TradeRequest request) {
        try {
            log.info("사용자 거래 실행: userId={}, stockCode={}, tradeType={}", 
                    userId, request.getStockCode(), request.getTradeType());
            
            // 기본 검증
            if (userId == null || request.getStockCode() == null || request.getTradeType() == null) {
                return TradeResponse.failure("INVALID_REQUEST", "필수 파라미터가 누락되었습니다.");
            }
            
            // 거래 요청 유효성 검증
            if (!request.isValidBuyRequest() && !request.isValidSellRequest()) {
                return TradeResponse.failure(userId, request.getStockCode(), request.getTradeType(),
                        "INVALID_TRADE_REQUEST", "유효하지 않은 거래 요청입니다.");
            }
            
            // MockTrade 엔티티 생성 (간소화 버전)
            MockTrade mockTrade = MockTrade.builder()
                    .stockCode(request.getStockCode())
                    .tradeType(request.getTradeType())
                    .quantity(request.getQuantity())
                    .price(request.getPrice() != null ? request.getPrice() : 75000) // 임시 기본값
                    .totalAmount((request.getPrice() != null ? request.getPrice() : 75000) * request.getQuantity())
                    .tradeTime(LocalDateTime.now())
                    .status("COMPLETED")
                    .build();
            
            // 거래 기록 저장
            MockTrade savedTrade = mockTradeRepository.save(mockTrade);
            
            log.info("사용자 거래 완료: tradeId={}", savedTrade.getTradeId());
            
            // 성공 응답 반환
            return TradeResponse.success(
                    savedTrade.getTradeId(),
                    userId,
                    savedTrade.getStockCode(),
                    "종목명", // TODO: StockInfo에서 실제 종목명 조회 필요
                    savedTrade.getTradeType(),
                    savedTrade.getQuantity(),
                    savedTrade.getPrice(),
                    savedTrade.getTotalAmount(),
                    0, // commission - TODO: 실제 수수료 계산 필요
                    0, // tax - TODO: 실제 세금 계산 필요
                    1000000, // remainingBalance - TODO: 실제 잔고 조회 필요
                    "거래가 성공적으로 처리되었습니다."
            );
            
        } catch (Exception e) {
            log.error("사용자 거래 실행 오류: {}", e.getMessage(), e);
            return TradeResponse.failure(userId, request.getStockCode(), request.getTradeType(),
                    "TRADE_EXECUTION_ERROR", "거래 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}

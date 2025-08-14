package com.example.LAGO.service;

import com.example.LAGO.domain.Account;
import com.example.LAGO.domain.Stock;
import com.example.LAGO.domain.StockHolding;
import com.example.LAGO.dto.response.StockHoldingResponse;
import com.example.LAGO.repository.AccountRepository;
import com.example.LAGO.repository.StockRepository;
import com.example.LAGO.repository.StockHoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 포트폴리오 서비스
 * 지침서 명세: 사용자 보유주식 조회 및 포트폴리오 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final StockHoldingRepository stockHoldingRepository;
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;

    /**
     * 사용자 포트폴리오 조회 (모든 계좌)
     * 지침서 명세: GET /api/users/me/portfolio
     * 
     * @param userId 사용자 ID
     * @return 포트폴리오 목록
     */
    @Transactional(readOnly = true)
    public List<StockHoldingResponse> getUserPortfolio(Long userId) {
        log.info("사용자 포트폴리오 조회: userId={}", userId);
        
        List<StockHolding> holdings = stockHoldingRepository.findByUserId(userId);
        
        return holdings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 계좌의 보유주식 조회
     * 지침서 명세: GET /api/accounts/{accountId}/holdings
     * 
     * @param accountId 계좌 ID
     * @param userId 사용자 ID (권한 확인용)
     * @return 보유주식 목록
     */
    @Transactional(readOnly = true)
    public List<StockHoldingResponse> getAccountHoldings(Long accountId, Long userId) {
        log.info("계좌별 보유주식 조회: accountId={}, userId={}", accountId, userId);
        
        // 계좌 소유자 확인
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다: " + accountId));
        
        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("해당 계좌에 대한 접근 권한이 없습니다.");
        }
        
        List<StockHolding> holdings = stockHoldingRepository.findByAccountId(accountId);
        
        return holdings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 종목의 보유 정보 조회
     * 
     * @param accountId 계좌 ID
     * @param stockCode 종목 코드
     * @param userId 사용자 ID (권한 확인용)
     * @return 보유주식 정보
     */
    @Transactional(readOnly = true)
    public StockHoldingResponse getStockHolding(Long accountId, String stockCode, Long userId) {
        log.info("종목별 보유 정보 조회: accountId={}, stockCode={}, userId={}", accountId, stockCode, userId);
        
        // 계좌 소유자 확인
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다: " + accountId));
        
        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("해당 계좌에 대한 접근 권한이 없습니다.");
        }
        
        StockHolding holding = stockHoldingRepository.findByAccountIdAndStockCode(accountId, stockCode)
                .orElseThrow(() -> new RuntimeException("보유하지 않은 종목입니다: " + stockCode));
        
        return convertToResponse(holding);
    }

    /**
     * StockHolding을 StockHoldingResponse로 변환
     */
    private StockHoldingResponse convertToResponse(StockHolding holding) {
        // 현재 주가 정보 조회
        Stock stock = stockRepository.findById(holding.getStockCode())
                .orElse(null);
        
        Integer currentPrice = stock != null ? stock.getCurrentPrice() : holding.getAveragePrice();
        
        // 현재 평가 금액 업데이트
        holding.updateCurrentValue(currentPrice);
        
        return StockHoldingResponse.builder()
                .holdingId(holding.getHoldingId())
                .stockCode(holding.getStockCode())
                .stockName(stock != null ? stock.getName() : "종목명 없음")
                .quantity(holding.getQuantity())
                .averagePrice(holding.getAveragePrice())
                .totalCost(holding.getTotalCost())
                .currentPrice(currentPrice)
                .currentValue(holding.getCurrentValue())
                .profitLoss(holding.getProfitLoss())
                .profitLossRate(holding.getProfitLossRate())
                .firstPurchaseDate(holding.getFirstPurchaseDate())
                .lastTradeDate(holding.getLastTradeDate())
                .market(stock != null ? stock.getMarket() : null)
                .sector(stock != null ? stock.getSector() : null)
                .build();
    }
}

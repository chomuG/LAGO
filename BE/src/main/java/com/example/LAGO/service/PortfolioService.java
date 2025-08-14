package com.example.LAGO.service;

import com.example.LAGO.domain.Account;
import com.example.LAGO.domain.Stock;
import com.example.LAGO.domain.StockHolding;
import com.example.LAGO.domain.StockInfo;
import com.example.LAGO.dto.response.AccountCurrentStatusResponse;
import com.example.LAGO.dto.response.StockHoldingResponse;
import com.example.LAGO.repository.AccountRepository;
import com.example.LAGO.repository.MockTradeRepository;
import com.example.LAGO.repository.StockRepository;
import com.example.LAGO.repository.StockHoldingRepository;
import com.example.LAGO.repository.StockInfoRepository;
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
    private final MockTradeRepository mockTradeRepository;
    private final StockInfoRepository stockInfoRepository;

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

    /**
     * 계좌 현재 상황 조회 (MockTrade 기반)
     * 프론트에서 실시간 계산을 위한 단순화된 데이터 제공
     * 
     * @param accountId 계좌 ID
     * @param userId 사용자 ID (권한 확인용)
     * @return 계좌 현재 상황
     */
    @Transactional(readOnly = true)
    public AccountCurrentStatusResponse getAccountCurrentStatus(Long accountId, Long userId) {
        log.info("계좌 현재 상황 조회: accountId={}, userId={}", accountId, userId);
        
        // 계좌 소유자 확인
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다: " + accountId));
        
        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("해당 계좌에 대한 접근 권한이 없습니다.");
        }
        
        // MockTrade에서 현재 보유 종목 정보 조회
        List<Object[]> holdingData = mockTradeRepository.findCurrentHoldingsByAccountId(accountId);
        
        // 보유 종목 정보 변환
        List<AccountCurrentStatusResponse.CurrentHoldingInfo> holdings = holdingData.stream()
                .map(data -> {
                    Integer stockId = (Integer) data[0];
                    Integer quantity = ((Number) data[1]).intValue();
                    Integer totalPurchaseAmount = ((Number) data[2]).intValue();
                    
                    // 종목 정보 조회
                    StockInfo stockInfo = stockInfoRepository.findById(stockId)
                            .orElseThrow(() -> new RuntimeException("종목 정보를 찾을 수 없습니다: " + stockId));
                    
                    return AccountCurrentStatusResponse.CurrentHoldingInfo.builder()
                            .stockCode(stockInfo.getCode())
                            .stockName(stockInfo.getName())
                            .quantity(quantity)
                            .totalPurchaseAmount(totalPurchaseAmount)
                            .build();
                })
                .collect(Collectors.toList());
        
        return AccountCurrentStatusResponse.builder()
                .accountId(accountId)
                .balance(account.getBalance())
                .profitRate(account.getProfitRate())
                .holdings(holdings)
                .build();
    }

    /**
     * 사용자 기본 계좌 현재 상황 조회 (MockTrade 기반)
     * userId로 타입 0인 기본 계좌를 찾아서 조회
     * 
     * @param userId 사용자 ID
     * @return 기본 계좌 현재 상황
     */
    @Transactional(readOnly = true)
    public AccountCurrentStatusResponse getUserCurrentStatus(Long userId) {
        log.info("사용자 기본 계좌 현재 상황 조회: userId={}", userId);
        
        // 타입 0인 기본 계좌 조회
        Account account = accountRepository.findByUserIdAndType(userId, Account.TYPE_MOCK_TRADING)
                .orElseThrow(() -> new RuntimeException("기본 계좌를 찾을 수 없습니다: userId=" + userId));
        
        // 기존 메서드 재사용
        return getAccountCurrentStatus(account.getAccountId(), userId);
    }
}

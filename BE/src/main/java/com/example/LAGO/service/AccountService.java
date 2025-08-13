package com.example.LAGO.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.LAGO.domain.Account;
import com.example.LAGO.dto.AccountDto;
import com.example.LAGO.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 계좌 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    
    private static final Integer MOCK_TRADING_INITIAL_BALANCE = 1000000; // 100만원
    private static final Integer HISTORICAL_CHALLENGE_INITIAL_BALANCE = 10000000; // 천만원
    private static final Integer MOCK_TRADING_TYPE = 0;
    private static final Integer HISTORICAL_CHALLENGE_TYPE = 1;

    /**
     * accountId로 계좌 단건 조회
     */
    public AccountDto getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "계좌를 찾을 수 없습니다. id=" + accountId));

        // Entity -> DTO 매핑 (DB/Entity 필드명 기준)
        return AccountDto.builder()
                .accountId(account.getAccountId())
                .userId(account.getUserId())
                .balance(account.getBalance())
                .totalAsset(account.getTotalAsset())
                .profit(account.getProfit())
                .profitRate(account.getProfitRate())
                .createdAt(account.getCreatedAt())
                .type(account.getType())
                .build();
    }
    
    /**
     * 신규 사용자용 계좌 두 개 생성 (모의투자 + 역사챌린지)
     */
    @Transactional
    public void createInitialAccountsForUser(Long userId) {
        createMockTradingAccount(userId);
        createHistoricalChallengeAccount(userId);
    }
    
    /**
     * 모의투자 계좌 생성
     */
    @Transactional
    public Account createMockTradingAccount(Long userId) {
        Account mockTradingAccount = Account.builder()
                .userId(userId)
                .balance(MOCK_TRADING_INITIAL_BALANCE)
                .totalAsset(MOCK_TRADING_INITIAL_BALANCE)
                .profit(0)
                .profitRate(0.0)
                .type(MOCK_TRADING_TYPE)
                .build();
        
        return accountRepository.save(mockTradingAccount);
    }
    
    /**
     * 역사챌린지 계좌 생성
     */
    @Transactional
    public Account createHistoricalChallengeAccount(Long userId) {
        Account historicalAccount = Account.builder()
                .userId(userId)
                .balance(HISTORICAL_CHALLENGE_INITIAL_BALANCE)
                .totalAsset(HISTORICAL_CHALLENGE_INITIAL_BALANCE)
                .profit(0)
                .profitRate(0.0)
                .type(HISTORICAL_CHALLENGE_TYPE)
                .build();
        
        return accountRepository.save(historicalAccount);
    }
}
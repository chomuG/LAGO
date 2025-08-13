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

    /**
     * accountId로 계좌 단건 조회
     */
    public AccountDto getAccountById(Integer accountId) {
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
}
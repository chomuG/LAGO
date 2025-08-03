package com.example.LAGO.service;

import com.example.LAGO.domain.User;
import com.example.LAGO.domain.Account;
import com.example.LAGO.repository.UserRepository;
import com.example.LAGO.repository.AccountRepository;
import com.example.LAGO.repository.MockTradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AiBotService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final MockTradeRepository mockTradeRepository;

    public Map<String, Object> getAiBotStatus(String personality) {
        User aiUser = userRepository.findByPersonalityAndIsDeleted(personality, true)
                .orElseThrow(() -> new RuntimeException("AI 봇 user를 찾을 수 없습니다."));

        Account aiAccount = accountRepository.findByUserIdAndType(aiUser.getUserId(), "ai_bot")
                .orElseThrow(() -> new RuntimeException("AI 봇 account를 찾을 수 없습니다."));

        Long tradeCount = mockTradeRepository.countByAccountId(aiAccount.getAccountId());
        Double avgTradeValue = mockTradeRepository.findAvgTradeValue(aiAccount.getAccountId());

        Map<String, Object> result = new HashMap<>();
        result.put("nickname", aiUser.getNickname());
        result.put("balance", aiAccount.getBalance());
        result.put("profitRate", aiAccount.getProfitRate());
        result.put("tradeCount", tradeCount);
        result.put("avgTradeValue", avgTradeValue);

        return result;
    }
}

package com.example.LAGO.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.LAGO.dto.AccountDto;
import com.example.LAGO.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 계좌 조회 API
 */
@Tag(name = "Account", description = "계좌 조회 API")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * GET /api/accounts/{accountId} - 계좌 단건 조회
     */
    @Operation(summary = "계좌 단건 조회", description = "accountId로 계좌 정보를 조회합니다.")
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable Integer accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }
}
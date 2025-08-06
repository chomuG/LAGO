package com.example.LAGO.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 계좌 정보 DTO
 * 지침서 명세: 연동된 EC2 DB ACCOUNT 테이블과 완전 일치
 * 
 * 연동된 EC2 DB 테이블 구조:
 * - account_id: PK (int)
 * - user_id: FK (int) 
 * - balance: 보유 현금 (int)
 * - total_asset: 총 자산 (int)
 * - profit: 수익 (int)
 * - profit_rate: 수익률 (float)
 * - created_at: 생성일 (datetime)
 * - type: 계좌구분 (varchar) - "현시점"/"역사챌린지"
 * 
 * @author D203팀 백엔드 개발자
 * @since 2025-08-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "계좌 정보")
public class AccountDto {

    /**
     * 계좌 ID (PK)
     */
    @Schema(description = "계좌 ID", example = "1001")
    private Integer accountId;

    /**
     * 사용자 ID (FK)
     */
    @Schema(description = "사용자 ID", example = "100")
    private Integer userId;

    /**
     * 보유 현금
     */
    @Schema(description = "보유 현금", example = "1000000")
    private Integer balance;

    /**
     * 총 자산 (현금 + 주식 평가액)
     */
    @Schema(description = "총 자산", example = "1500000")
    private Integer totalAsset;

    /**
     * 수익 (총 자산 - 초기 자본)
     */
    @Schema(description = "수익", example = "500000")
    private Integer profit;

    /**
     * 수익률 (%)
     */
    @Schema(description = "수익률", example = "50.0")
    private Float profitRate;

    /**
     * 계좌 생성일
     */
    @Schema(description = "계좌 생성일", example = "2025-01-01T10:00:00")
    private LocalDateTime createdAt;

    /**
     * 계좌 구분
     * - "현시점": 일반 모의투자 계좌
     * - "역사챌린지": 역사적 시점 시뮬레이션 계좌
     */
    @Schema(description = "계좌 구분", example = "현시점", allowableValues = {"현시점", "역사챌린지"})
    private String type;

    /**
     * 계좌 요약 정보 생성 (포트폴리오용)
     * 
     * @param accountId 계좌 ID
     * @param balance 보유 현금
     * @param totalAsset 총 자산
     * @param profit 수익
     * @param profitRate 수익률
     * @return 계좌 요약 DTO
     */
    public static AccountDto createSummary(Integer accountId, Integer balance, 
                                         Integer totalAsset, Integer profit, Float profitRate) {
        return AccountDto.builder()
                .accountId(accountId)
                .balance(balance)
                .totalAsset(totalAsset)
                .profit(profit)
                .profitRate(profitRate)
                .build();
    }

    /**
     * 계좌 생성 요청 DTO (회원가입 시 사용)
     * 
     * @param userId 사용자 ID
     * @param type 계좌 구분
     * @param initialBalance 초기 자본
     * @return 계좌 생성 DTO
     */
    public static AccountDto createRequest(Integer userId, String type, Integer initialBalance) {
        return AccountDto.builder()
                .userId(userId)
                .type(type)
                .balance(initialBalance)
                .totalAsset(initialBalance)
                .profit(0)
                .profitRate(0.0f)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 투자 수익률 계산 및 업데이트
     * 
     * @param currentStockValue 현재 주식 평가액
     * @param initialCapital 초기 자본
     */
    public void updateProfitAndRate(Integer currentStockValue, Integer initialCapital) {
        this.totalAsset = this.balance + currentStockValue;
        this.profit = this.totalAsset - initialCapital;
        this.profitRate = initialCapital > 0 ? 
            ((float) this.profit / initialCapital) * 100 : 0.0f;
    }

    /**
     * 계좌 상태 검증
     * 
     * @return 유효한 계좌인지 여부
     */
    public boolean isValid() {
        return accountId != null && accountId > 0 &&
               userId != null && userId > 0 &&
               balance != null && balance >= 0 &&
               totalAsset != null && totalAsset >= 0 &&
               type != null && !type.trim().isEmpty();
    }

    /**
     * 거래 가능한 잔액 확인
     * 
     * @param requiredAmount 필요 금액
     * @return 거래 가능 여부
     */
    public boolean hasEnoughBalance(Integer requiredAmount) {
        return balance != null && requiredAmount != null && 
               balance >= requiredAmount;
    }
}

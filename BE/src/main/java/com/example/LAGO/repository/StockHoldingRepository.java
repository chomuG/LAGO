package com.example.LAGO.repository;

import com.example.LAGO.domain.StockHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * StockHolding 엔티티 Repository
 * 지침서 명세: STOCK_HOLDING 테이블 연동
 */
@Repository
public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {

    /**
     * 계좌별 보유 주식 조회
     * @param accountId 계좌 ID
     * @return 보유 주식 목록
     */
    @Query("SELECT sh FROM StockHolding sh WHERE sh.account.accountId = :accountId AND sh.quantity > 0")
    List<StockHolding> findByAccountId(@Param("accountId") Integer accountId);

    /**
     * 특정 계좌의 특정 종목 보유 정보 조회
     * @param accountId 계좌 ID
     * @param stockCode 종목 코드
     * @return 보유 주식 정보
     */
    @Query("SELECT sh FROM StockHolding sh WHERE sh.account.accountId = :accountId AND sh.stockCode = :stockCode")
    Optional<StockHolding> findByAccountIdAndStockCode(@Param("accountId") Integer accountId, @Param("stockCode") String stockCode);

    /**
     * 사용자별 보유 주식 조회 (모든 계좌)
     * @param userId 사용자 ID
     * @return 보유 주식 목록
     */
    @Query("SELECT sh FROM StockHolding sh WHERE sh.account.userId = :userId AND sh.quantity > 0")
    List<StockHolding> findByUserId(@Param("userId") Integer userId);

    /**
     * 사용자와 종목으로 보유 주식 조회 (AI 봇용)
     * @param userId 사용자 ID
     * @param stockCode 종목 코드
     * @return 보유 주식 목록
     */
    @Query("SELECT sh FROM StockHolding sh WHERE sh.account.userId = :userId AND sh.stockCode = :stockCode AND sh.quantity > 0")
    List<StockHolding> findByUserIdAndStockCode(@Param("userId") Integer userId, @Param("stockCode") String stockCode);

    /**
     * 특정 종목을 보유한 모든 계좌 조회
     * @param stockCode 종목 코드
     * @return 해당 종목 보유 목록
     */
    @Query("SELECT sh FROM StockHolding sh WHERE sh.stockCode = :stockCode AND sh.quantity > 0")
    List<StockHolding> findByStockCode(@Param("stockCode") String stockCode);

    /**
     * 계좌의 총 보유 종목 수 조회
     * @param accountId 계좌 ID
     * @return 보유 종목 수
     */
    @Query("SELECT COUNT(sh) FROM StockHolding sh WHERE sh.account.accountId = :accountId AND sh.quantity > 0")
    Long countByAccountId(@Param("accountId") Integer accountId);

    /**
     * 계좌의 총 투자 금액 조회
     * @param accountId 계좌 ID
     * @return 총 투자 금액
     */
    @Query("SELECT COALESCE(SUM(sh.totalCost), 0) FROM StockHolding sh WHERE sh.account.accountId = :accountId AND sh.quantity > 0")
    Long getTotalInvestmentByAccountId(@Param("accountId") Integer accountId);

    /**
     * 계좌의 현재 평가 금액 조회
     * @param accountId 계좌 ID
     * @return 현재 평가 금액
     */
    @Query("SELECT COALESCE(SUM(sh.currentValue), 0) FROM StockHolding sh WHERE sh.account.accountId = :accountId AND sh.quantity > 0")
    Long getCurrentValueByAccountId(@Param("accountId") Integer accountId);

    /**
     * 수익률 기준 상위 보유 종목 조회
     * @param accountId 계좌 ID
     * @param limit 조회 개수
     * @return 수익률 상위 종목 목록
     */
    @Query("SELECT sh FROM StockHolding sh WHERE sh.account.accountId = :accountId AND sh.quantity > 0 ORDER BY sh.profitLossRate DESC")
    List<StockHolding> findTopProfitableStocks(@Param("accountId") Integer accountId, @Param("limit") int limit);

    /**
     * 손실률 기준 하위 보유 종목 조회
     * @param accountId 계좌 ID
     * @param limit 조회 개수
     * @return 손실률 하위 종목 목록
     */
    @Query("SELECT sh FROM StockHolding sh WHERE sh.account.accountId = :accountId AND sh.quantity > 0 ORDER BY sh.profitLossRate ASC")
    List<StockHolding> findTopLosingStocks(@Param("accountId") Integer accountId, @Param("limit") int limit);
}

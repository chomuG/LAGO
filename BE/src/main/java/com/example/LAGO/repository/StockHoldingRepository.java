package com.example.LAGO.repository;

import com.example.LAGO.domain.StockHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 보유 주식 레포지토리
 */
@Repository
public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {

    /**
     * 사용자 ID로 보유 주식 조회
     */
    @Query("SELECT sh FROM StockHolding sh WHERE sh.accountId IN " +
           "(SELECT a.accountId FROM Account a WHERE a.userId = :userId)")
    List<StockHolding> findByUserId(@Param("userId") Long userId);

    /**
     * 계좌 ID로 보유 주식 조회
     */
    List<StockHolding> findByAccountId(Long accountId);

    /**
     * 계좌 ID와 종목 코드로 보유 주식 조회
     */
    Optional<StockHolding> findByAccountIdAndStockCode(Long accountId, String stockCode);
}
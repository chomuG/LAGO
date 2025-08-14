package com.example.LAGO.repository;

import com.example.LAGO.domain.MockTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MockTradeRepository extends JpaRepository<MockTrade, Long> {
    @Query("SELECT COUNT(mt) FROM MockTrade mt WHERE mt.accountId = :accountId")
    Long countByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT AVG(mt.price * mt.quantity) FROM MockTrade mt WHERE mt.accountId = :accountId")
    Double findAvgTradeValue(@Param("accountId") Long accountId);

    /**
     * 계좌별 현재 보유 종목 정보 조회
     * 매수/매도를 계산해서 현재 보유량이 0보다 큰 종목만 반환
     */
    @Query("""
        SELECT mt.stockId,
               SUM(CASE WHEN mt.tradeType = 'BUY' THEN mt.quantity ELSE -mt.quantity END) as currentQuantity,
               SUM(CASE WHEN mt.tradeType = 'BUY' THEN mt.price * mt.quantity ELSE -mt.price * mt.quantity END) as totalPurchaseAmount
        FROM MockTrade mt 
        WHERE mt.accountId = :accountId
        GROUP BY mt.stockId
        HAVING SUM(CASE WHEN mt.tradeType = 'BUY' THEN mt.quantity ELSE -mt.quantity END) > 0
        """)
    List<Object[]> findCurrentHoldingsByAccountId(@Param("accountId") Long accountId);
}

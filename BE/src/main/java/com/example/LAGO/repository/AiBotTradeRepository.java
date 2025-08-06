package com.example.LAGO.repository;

import com.example.LAGO.domain.AiBotTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 봇 거래 Repository
 * 지침서 명세: AI_BOT_TRADE 테이블 연동
 */
@Repository
public interface AiBotTradeRepository extends JpaRepository<AiBotTrade, Long> {

    /**
     * 사용자별 AI 봇 거래 내역 조회
     * @param userId 사용자 ID
     * @return 거래 내역 목록
     */
    List<AiBotTrade> findByUserIdOrderByTradeTimeDesc(Integer userId);

    /**
     * 전략별 거래 내역 조회
     * @param strategyId 전략 ID
     * @return 거래 내역 목록
     */
    @Query("SELECT bt FROM AiBotTrade bt WHERE bt.strategy.strategyId = :strategyId ORDER BY bt.tradeTime DESC")
    List<AiBotTrade> findByStrategyIdOrderByTradeTimeDesc(@Param("strategyId") Long strategyId);

    /**
     * 종목별 AI 봇 거래 내역 조회
     * @param stockCode 종목 코드
     * @return 거래 내역 목록
     */
    List<AiBotTrade> findByStockCodeOrderByTradeTimeDesc(String stockCode);

    /**
     * 거래 결과별 조회
     * @param result 거래 결과 (SUCCESS/FAILED/PENDING)
     * @return 거래 내역 목록
     */
    List<AiBotTrade> findByResult(String result);

    /**
     * 기간별 거래 내역 조회
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @return 거래 내역 목록
     */
    @Query("SELECT bt FROM AiBotTrade bt WHERE bt.tradeTime BETWEEN :startTime AND :endTime ORDER BY bt.tradeTime DESC")
    List<AiBotTrade> findByTradeTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 사용자 및 전략별 거래 내역 조회
     * @param userId 사용자 ID
     * @param strategyId 전략 ID
     * @return 거래 내역 목록
     */
    @Query("SELECT bt FROM AiBotTrade bt WHERE bt.userId = :userId AND bt.strategy.strategyId = :strategyId ORDER BY bt.tradeTime DESC")
    List<AiBotTrade> findByUserIdAndStrategyId(@Param("userId") Integer userId, @Param("strategyId") Long strategyId);

    /**
     * 성공한 매수 거래 조회 (매도용)
     * @param userId 사용자 ID
     * @param stockCode 종목 코드
     * @return 매수 거래 목록
     */
    @Query("SELECT bt FROM AiBotTrade bt WHERE bt.userId = :userId AND bt.stockCode = :stockCode AND bt.tradeType = 'BUY' AND bt.result = 'SUCCESS' ORDER BY bt.tradeTime ASC")
    List<AiBotTrade> findBuyTradesForSell(@Param("userId") Integer userId, @Param("stockCode") String stockCode);

    /**
     * 전략별 수익률 통계
     * @param strategyId 전략 ID
     * @return 평균 수익률
     */
    @Query("SELECT AVG(bt.profitLossRate) FROM AiBotTrade bt WHERE bt.strategy.strategyId = :strategyId AND bt.tradeType = 'SELL' AND bt.result = 'SUCCESS'")
    Float getAverageReturnByStrategy(@Param("strategyId") Long strategyId);

    /**
     * 전략별 총 수익 계산
     * @param strategyId 전략 ID
     * @return 총 수익
     */
    @Query("SELECT COALESCE(SUM(bt.profitLoss), 0) FROM AiBotTrade bt WHERE bt.strategy.strategyId = :strategyId AND bt.tradeType = 'SELL' AND bt.result = 'SUCCESS'")
    Long getTotalProfitByStrategy(@Param("strategyId") Long strategyId);

    /**
     * 전략별 거래 성공률
     * @param strategyId 전략 ID
     * @return 성공률 (%)
     */
    @Query("SELECT (COUNT(CASE WHEN bt.result = 'SUCCESS' THEN 1 END) * 100.0 / COUNT(*)) FROM AiBotTrade bt WHERE bt.strategy.strategyId = :strategyId")
    Float getSuccessRateByStrategy(@Param("strategyId") Long strategyId);

    /**
     * 일별 거래 통계
     * @param date 날짜
     * @return 해당 날짜의 거래 목록
     */
    @Query("SELECT bt FROM AiBotTrade bt WHERE DATE(bt.tradeTime) = DATE(:date)")
    List<AiBotTrade> findByTradeDate(@Param("date") LocalDateTime date);

    /**
     * 최근 거래 내역 조회 (제한)
     * @param userId 사용자 ID
     * @param limit 조회 개수
     * @return 최근 거래 목록
     */
    @Query("SELECT bt FROM AiBotTrade bt WHERE bt.userId = :userId ORDER BY bt.tradeTime DESC LIMIT :limit")
    List<AiBotTrade> findRecentTradesByUser(@Param("userId") Integer userId, @Param("limit") int limit);

    /**
     * RSI 신호 기반 거래 조회
     * @param minRsi 최소 RSI
     * @param maxRsi 최대 RSI
     * @return RSI 범위 내 거래 목록
     */
    @Query("SELECT bt FROM AiBotTrade bt WHERE bt.rsiValue BETWEEN :minRsi AND :maxRsi AND bt.result = 'SUCCESS'")
    List<AiBotTrade> findByRsiRange(@Param("minRsi") Float minRsi, @Param("maxRsi") Float maxRsi);

    /**
     * 골든크로스 신호 거래 조회
     * @return 골든크로스 신호로 실행된 거래 목록
     */
    @Query("SELECT bt FROM AiBotTrade bt WHERE bt.log LIKE '%골든크로스%' AND bt.result = 'SUCCESS'")
    List<AiBotTrade> findGoldenCrossTrades();
}

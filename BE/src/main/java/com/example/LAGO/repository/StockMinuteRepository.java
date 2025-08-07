package com.example.LAGO.repository;

import com.example.LAGO.domain.StockMinute;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMinuteRepository extends JpaRepository<StockMinute, Integer>{
    // 구간 조회 (stock_info_id, 시작일~종료일)
    List<StockMinute> findByStockInfoStockInfoIdAndDateBetweenOrderByDateAsc(
            Integer stockInfoId,
            LocalDateTime start,
            LocalDateTime end
    );
    // ✅ 추가: 실시간 종가 기준 최근 2개 조회
    @Query("""
        SELECT sm FROM StockMinute sm
        WHERE sm.stockInfo.stockInfoId = :stockInfoId
        AND sm.date < CURRENT_TIMESTAMP
        ORDER BY sm.date DESC
    """)
    List<StockMinute> findRecentTwoByStockInfoId(
            @Param("stockInfoId") Integer stockInfoId,
            Pageable pageable
    );

    // 특정 주식의 거래시간 내 최신 종가 조회 (현재 시간 기준 또는 15:30 이후엔 15:30 데이터)
    @Query("""
        SELECT sm FROM StockMinute sm
        WHERE sm.stockInfo.stockInfoId = :stockInfoId
        AND sm.date >= :startTime
        AND sm.date <= :endTime
        ORDER BY sm.date DESC
    """)
    List<StockMinute> findCurrentTradingPrice(
            @Param("stockInfoId") Integer stockInfoId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 특정 주식의 이전 시간 종가 조회
    @Query("""
        SELECT sm FROM StockMinute sm
        WHERE sm.stockInfo.stockInfoId = :stockInfoId
        AND sm.date >= :startTime
        AND sm.date < :currentTime
        ORDER BY sm.date DESC
    """)
    List<StockMinute> findPreviousTradingPrice(
            @Param("stockInfoId") Integer stockInfoId,
            @Param("startTime") LocalDateTime startTime,
            @Param("currentTime") LocalDateTime currentTime
    );

}

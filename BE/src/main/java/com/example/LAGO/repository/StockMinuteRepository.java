package com.example.LAGO.repository;

import com.example.LAGO.domain.StockDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 주식 분단위 데이터 Repository
 * 
 * 실시간 주가 분석을 위한 분단위 데이터 처리
 * 현재는 StockDay 테이블을 활용하여 일단위 데이터로 대체
 * 
 * @author 라고할때 팀
 * @version 1.0
 * @since 2025-01-04
 */
@Repository
public interface StockMinuteRepository extends JpaRepository<StockDay, Long> {
    
    /**
     * 종목코드로 최근 데이터 조회 (분단위 대신 일단위 사용)
     */
    @Query("SELECT s FROM StockDay s WHERE s.stockInfo.code = :stockCode ORDER BY s.date DESC")
    List<StockDay> findByStockCodeOrderByDateDesc(@Param("stockCode") String stockCode);
    
    /**
     * 종목코드와 날짜 범위로 데이터 조회
     */
    @Query("SELECT s FROM StockDay s WHERE s.stockInfo.code = :stockCode AND s.date BETWEEN :startDate AND :endDate ORDER BY s.date ASC")
    List<StockDay> findByStockCodeAndDateBetween(@Param("stockCode") String stockCode, 
                                                @Param("startDate") LocalDate startDate, 
                                                @Param("endDate") LocalDate endDate);
    
    /**
     * 최근 N일 데이터 조회
     */
    @Query(value = "SELECT * FROM STOCK_DAY sd " +
           "JOIN STOCK_INFO si ON sd.stock_info_id = si.stock_info_id " +
           "WHERE si.code = :stockCode " +
           "ORDER BY sd.date DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<StockDay> findRecentDataByStockCode(@Param("stockCode") String stockCode, @Param("limit") int limit);
}

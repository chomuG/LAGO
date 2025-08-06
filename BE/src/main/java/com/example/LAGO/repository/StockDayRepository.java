package com.example.LAGO.repository;

import com.example.LAGO.domain.StockDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * StockDay 엔티티 Repository
 * 연동된 EC2 DB STOCK_DAY 테이블 접근
 */
@Repository
public interface StockDayRepository extends JpaRepository<StockDay, Integer> {

    /**
     * 종목 정보 ID와 날짜로 일봉 데이터 조회
     * @param stockInfoId 종목 정보 ID
     * @param date 거래일
     * @return 일봉 데이터
     */
    Optional<StockDay> findByStockInfoIdAndDate(Integer stockInfoId, LocalDate date);

    /**
     * 종목 정보 ID로 최근 N일 일봉 데이터 조회 (최신순)
     * @param stockInfoId 종목 정보 ID
     * @param limit 조회할 일수
     * @return 일봉 데이터 리스트 (최신순)
     */
    @Query("SELECT sd FROM StockDay sd WHERE sd.stockInfoId = :stockInfoId " +
           "ORDER BY sd.date DESC LIMIT :limit")
    List<StockDay> findByStockInfoIdOrderByDateDescLimit(@Param("stockInfoId") Integer stockInfoId, 
                                                        @Param("limit") int limit);

    /**
     * 종목 정보 ID로 특정 기간 일봉 데이터 조회
     * @param stockInfoId 종목 정보 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 일봉 데이터 리스트 (날짜순)
     */
    List<StockDay> findByStockInfoIdAndDateBetweenOrderByDateAsc(Integer stockInfoId, 
                                                                LocalDate startDate, 
                                                                LocalDate endDate);

    /**
     * 종목 정보 ID로 최신 일봉 데이터 조회
     * @param stockInfoId 종목 정보 ID
     * @return 최신 일봉 데이터
     */
    Optional<StockDay> findTopByStockInfoIdOrderByDateDesc(Integer stockInfoId);

    /**
     * 특정 날짜의 모든 종목 일봉 데이터 조회
     * @param date 거래일
     * @return 일봉 데이터 리스트
     */
    List<StockDay> findByDateOrderByStockInfoIdAsc(LocalDate date);
}

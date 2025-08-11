package com.example.LAGO.repository;

import com.example.LAGO.domain.StockInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

public interface StockInfoRepository extends JpaRepository<StockInfo, Integer> {
    /**
     * 종목 코드로 주식 정보 조회
     *
     * @param code 종목 코드 (예: 005930)
     * @return 주식 정보
     */
    Optional<StockInfo> findByCode(String code);

    /**
     * 종목명으로 주식 정보 조회
     *
     * @param name 종목명
     * @return 주식 정보
     */
    Optional<StockInfo> findByName(String name);

    /**
     * 시장별 주식 정보 조회
     *
     * @param market 시장 구분 (KOSPI/KOSDAQ)
     * @return 주식 정보 리스트
     */
    java.util.List<StockInfo> findByMarket(String market);

    // 종목코드로 stock_info_id만 직접 조회 (성능 최적화)
    @Query("SELECT s.stockInfoId FROM StockInfo s WHERE s.code = :code")
    Optional<Integer> findStockInfoIdByCode(@Param("code") String code);

    // 캐싱을 위한 존재 여부 확인
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM StockInfo s WHERE s.code = :code")
    boolean existsByCode(@Param("code") String code);


}
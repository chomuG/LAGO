package com.example.LAGO.repository;

import com.example.LAGO.domain.StockInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

public interface StockInfoRepository extends JpaRepository<StockInfo, Integer> {
    // 종목코드로 StockInfo 조회 (필수!) - stock_info_id 반환용
    @Query("SELECT s FROM StockInfo s WHERE s.code = :code")
    Optional<StockInfo> findByCode(@Param("code") String code);

    // 종목코드로 stock_info_id만 직접 조회 (성능 최적화)
    @Query("SELECT s.stockInfoId FROM StockInfo s WHERE s.code = :code")
    Optional<Integer> findStockInfoIdByCode(@Param("code") String code);

    // 캐싱을 위한 존재 여부 확인
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM StockInfo s WHERE s.code = :code")
    boolean existsByCode(@Param("code") String code);

}

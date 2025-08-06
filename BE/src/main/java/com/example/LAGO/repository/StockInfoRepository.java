package com.example.LAGO.repository;

import com.example.LAGO.domain.StockInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * StockInfo 엔티티 Repository
 * 연동된 EC2 DB STOCK_INFO 테이블 접근
 */
@Repository
public interface StockInfoRepository extends JpaRepository<StockInfo, Integer> {

    /**
     * 종목 코드로 주식 정보 조회
     * @param code 종목 코드 (예: 005930)
     * @return 주식 정보
     */
    Optional<StockInfo> findByCode(String code);

    /**
     * 종목명으로 주식 정보 조회
     * @param name 종목명
     * @return 주식 정보
     */
    Optional<StockInfo> findByName(String name);

    /**
     * 시장별 주식 정보 조회
     * @param market 시장 구분 (KOSPI/KOSDAQ)
     * @return 주식 정보 리스트
     */
    java.util.List<StockInfo> findByMarket(String market);
}

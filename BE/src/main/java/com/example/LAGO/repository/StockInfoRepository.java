package com.example.LAGO.repository;

import com.example.LAGO.domain.StockInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 주식 정보 레포지토리
 */
@Repository
public interface StockInfoRepository extends JpaRepository<StockInfo, Long> {

    /**
     * 종목 코드로 주식 정보 조회
     */
    Optional<StockInfo> findByCode(String code);
}
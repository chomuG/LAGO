package com.example.LAGO.repository;

import com.example.LAGO.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 주식 현재가 레포지토리
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, String> {

    /**
     * 종목 코드로 주식 현재가 조회
     */
    Optional<Stock> findByCode(String code);
}
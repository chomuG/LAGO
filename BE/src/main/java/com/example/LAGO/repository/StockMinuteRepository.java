package com.example.LAGO.repository;

import com.example.LAGO.domain.StockInfo;
import com.example.LAGO.domain.StockMinute;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMinuteRepository extends JpaRepository<StockMinute, Integer> {
    // 구간 조회 (stock_info_id, 시작일~종료일)
    List<StockMinute> findByStockInfoAndDateBetweenOrderByDateAsc(
            StockInfo stockInfoId,
            LocalDateTime start,
            LocalDateTime end
    );

    // 특정 종목의, 특정 시간 분 데이터(최신 시간) 한 건 조회
    StockMinute findTopByStockInfoAndDateOrderByDateDesc(StockInfo stockInfoId, LocalDateTime date);


}
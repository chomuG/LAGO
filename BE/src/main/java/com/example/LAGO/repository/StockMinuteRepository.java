package com.example.LAGO.repository;

import com.example.LAGO.domain.StockMinute;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface StockMinuteRepository extends JpaRepository<StockMinute, Integer>{
    // 구간 조회 (stock_info_id, 시작일~종료일)
    List<StockMinute> findByStockInfo_StockInfoIdAndDateBetweenOrderByDateAsc(
            Integer stockInfoId,
            LocalDateTime start,
            LocalDateTime end
    );
}

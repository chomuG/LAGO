package com.example.LAGO.repository;

import com.example.LAGO.entity.StockDay;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface StockDayRepository extends JpaRepository<StockDay, Integer> {
    // stock_info_id, date 구간, 날짜 오름차순 조회
    List<StockDay> findByStockInfo_StockInfoIdAndDateBetweenOrderByDateAsc(
            Integer stockInfoId,
            LocalDate start,
            LocalDate end
    );
}

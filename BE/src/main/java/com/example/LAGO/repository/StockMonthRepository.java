package com.example.LAGO.repository;

import com.example.LAGO.entity.StockMonth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockMonthRepository extends JpaRepository<StockMonth, Integer> {
    // int 타입 날짜 구간, 오름차순 조회
    List<StockMonth> findByStockInfo_StockInfoIdAndDateBetweenOrderByDateAsc(
            Integer stockInfoId,
            Integer start,
            Integer end
    );
}

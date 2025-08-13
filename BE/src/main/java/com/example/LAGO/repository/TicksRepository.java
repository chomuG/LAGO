package com.example.LAGO.repository;

import com.example.LAGO.domain.StockInfo;
import com.example.LAGO.domain.StockMinute;
import com.example.LAGO.domain.Ticks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicksRepository extends JpaRepository<Ticks, Integer> {

    @Query(value = "SELECT " +
                "time_bucket(CAST(:interval AS INTERVAL), t.ts) AS bucket, " +
                "FIRST(t.open_price, t.ts) AS open, " +
                "MAX(t.high_price) AS high, " +
                "MIN(t.low_price) AS low, " +
                "LAST(t.close_price, t.ts) AS close, " +
                "SUM(t.volume) AS volume " +
            "FROM ticks t " +
                "JOIN stock_info si ON t.stock_info_id = si.stock_info_id " +
            "WHERE si.code = :stockCode " +
                "AND t.ts BETWEEN :fromDateTime AND :toDateTime " +
            "GROUP BY bucket " +
            "ORDER BY bucket", nativeQuery = true)
    List<Object[]> findAggregatedByStockCodeAndDate(
            @Param("stockCode") String stockCode,
            @Param("interval") String interval,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime
    );

}
package com.example.LAGO.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "STOCK_MONTH")
@Getter
@Setter
@NoArgsConstructor
public class StockMonth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_month_id")
    private Integer stockMonthId;

    @ManyToOne
    @JoinColumn(name = "stock_info_id", nullable = false)
    private StockInfo stockInfo;

    @Column(name = "date", nullable = false)
    private Integer date;

    @Column(name = "open_price", nullable = false)
    private Integer openPrice;

    @Column(name = "high_price", nullable = false)
    private Integer highPrice;

    @Column(name = "low_price", nullable = false)
    private Integer lowPrice;

    @Column(name = "close_price", nullable = false)
    private Integer closePrice;

    @Column(name = "fluctuation_rate")
    private Float fluctuationRate;

    @Column(name = "volume", nullable = false)
    private Long volume;

}

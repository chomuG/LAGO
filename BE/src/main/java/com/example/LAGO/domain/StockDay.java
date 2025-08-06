package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "STOCK_DAY")
@Getter
@Setter
@NoArgsConstructor
public class StockDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_day_id")
    private Integer stockDayId;

    @ManyToOne
    @JoinColumn(name = "stock_info_id", nullable = false)
    private StockInfo stockInfo;

    @Column(name = "date", nullable = false)
    private LocalDate date;

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
    private Integer volume;
}


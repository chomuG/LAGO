package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 주식 현재가 정보 엔티티
 * STOCK 테이블과 매핑
 */
@Entity
@Table(name = "STOCK")
@Getter
@Setter
@NoArgsConstructor
public class Stock {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "current_price")
    private Integer currentPrice;

    @Column(name = "market")
    private String market;

    @Column(name = "sector")
    private String sector;

    @Column(name = "open_price")
    private Integer openPrice;

    @Column(name = "high_price")
    private Integer highPrice;

    @Column(name = "low_price")
    private Integer lowPrice;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "change_rate")
    private Double changeRate;

    @Column(name = "change_amount")
    private Integer changeAmount;
}
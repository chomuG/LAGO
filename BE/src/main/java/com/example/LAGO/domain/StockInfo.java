package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 주식 정보 엔티티
 * STOCK_INFO 테이블과 매핑
 */
@Entity
@Table(name = "STOCK_INFO")
@Getter
@Setter
@NoArgsConstructor
public class StockInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_info_id")
    private Long stockInfoId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "market")
    private String market;

    @Column(name = "sector")
    private String sector;

    @Column(name = "industry")
    private String industry;

    @Column(name = "listing_date")
    private String listingDate;

    @Column(name = "settle_month")
    private String settleMonth;

    @Column(name = "representative")
    private String representative;

    @Column(name = "homepage")
    private String homepage;

    @Column(name = "region")
    private String region;
}
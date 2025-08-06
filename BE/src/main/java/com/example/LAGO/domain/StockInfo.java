package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 주식 정보 엔티티
 * 연동된 EC2 DB STOCK_INFO 테이블과 완전 일치
 * 
 * 테이블 구조:
 * - stock_info_id: PK (auto_increment)
 * - code: 종목 코드 (varchar(20))
 * - name: 종목명 (varchar(100))
 * - market: 시장구분 (varchar(20))
 */
@Entity
@Table(name = "STOCK_INFO")
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockInfo {

    /**
     * 주식 정보 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_info_id")
    private Integer stockInfoId;

    /**
     * 종목 코드 (예: 005930)
     */
    @Column(name = "code", length = 20)
    private String code;

    /**
     * 종목명 (예: 삼성전자)
     */
    @Column(name = "name", length = 100)
    private String name;

    /**
     * 시장 구분 (KOSPI/KOSDAQ)
     */
    @Column(name = "market", length = 20)
    private String market;
}

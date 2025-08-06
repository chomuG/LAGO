package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "STOCK_INFO")
@Getter
@Setter
@NoArgsConstructor
public class StockInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_info_id")
    private Integer stockInfoId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "market")
    private String market;

    // 생성자
    public StockInfo(String code, String name, String market) {
        this.code = code;
        this.name = name;
        this.market = market;
    }
}

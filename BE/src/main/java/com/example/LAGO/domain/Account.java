package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 계좌 엔티티
 * 지침서 명세 ACCOUNTS 테이블과 완전 일치
 */
@Entity
@Table(name = "ACCOUNTS")
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "balance", nullable = false)
    private Integer balance;

    @Column(name = "total_asset", nullable = false)
    private Integer totalAsset;

    @Column(name = "profit", nullable = false)
    private Integer profit;

    @Column(name = "profit_rate", nullable = false)
    private Float profitRate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "type", nullable = false)
    private String type; // 계좌구분(현시점/역사챌린지/ai_bot)

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

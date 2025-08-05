package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MOCK_TRADE")
@Getter @Setter
public class MockTrade {

    @Id
    @Column(name = "trade_id")
    private Long tradeId;

    @Column(name = "account_id")
    private Long accountId;

    private Integer price;
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;
}

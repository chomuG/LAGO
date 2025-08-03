package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "account")
@Getter @Setter
public class Account {

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "user_id")
    private Long userId;

    private Integer balance;

    @Column(name = "profit_rate")
    private Float profitRate;

    private String type;
}

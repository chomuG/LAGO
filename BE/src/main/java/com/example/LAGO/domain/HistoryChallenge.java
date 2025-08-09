package com.example.LAGO.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 역사챌린지 엔티티
 */
@Entity
@Table(name = "\"HISTORY_CHALLENGE\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryChallenge {

    @Id
    @Column(name = "challenge_id")
    private Integer challengeId;

    @Column(name = "theme")
    private String theme;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;
}

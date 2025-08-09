package com.example.LAGO.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 역사챌린지 엔티티
 */
@Entity
@Table(name = "\"HISTORY_CHALLENGE_DATA\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryChallengeData {

    @Id
    @Column(name = "challenge_data_id")
    private Integer challengeDataId;

    @Column(name = "challenge_id")
    private Integer challengeId;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "open_price", nullable = false)
    private Integer openPrice;

    @Column(name = "high_price", nullable = false)
    private Integer highPrice;

    @Column(name = "low_price", nullable = false)
    private Integer lowPrice;

    @Column(name = "close_price", nullable = false)
    private Integer closePrice;

    @Column(name = "fluctuation_rate", nullable = false)
    private Float fluctuationRate;

    @Column(name = "volume", nullable = false)
    private Integer volume;

    @Column(name = "interval_type")
    private String intervalType;

    @Column(name = "start_origin_date")
    private LocalDateTime startOriginDate;

    @Column(name = "end_origin_date")
    private LocalDateTime endOriginDate;
}

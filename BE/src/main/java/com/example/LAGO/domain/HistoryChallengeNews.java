package com.example.LAGO.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"HISTORY_CHALLENGE_NEWS\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryChallengeNews {

    @Id
    @Column(name = "challenge_news_id")
    private Integer challengeNewsId;

    @Column(name = "challenge_id")
    private Integer challengeId;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
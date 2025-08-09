package com.example.LAGO.repository;

import com.example.LAGO.domain.HistoryChallengeData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoryChallengeDataRepository {
    /**
     * 특정 날짜까지의 역사챌린지 주가 데이터를 조회합니다.
     *
     * @param challengeId 챌린지 ID
     * @param interval 간격
     * @param targetDate 조회할 날짜
     * @return 역사챌린지 주가 데이터 목록
     */
    @Query("SELECT hc FROM HistoryChallengeData hc WHERE hc.challengeId = :challengeId and hc.intervalType = :interval and :targetDate <= hc.eventDate")
    List<HistoryChallengeData> findByIntervalTypeAndDate(Integer challengeId, String interval, LocalDateTime targetDate);
}

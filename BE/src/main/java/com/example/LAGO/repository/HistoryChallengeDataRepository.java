package com.example.LAGO.repository;

import com.example.LAGO.domain.HistoryChallengeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoryChallengeDataRepository extends JpaRepository<HistoryChallengeData, Integer> {

    @Query("SELECT hcd FROM HistoryChallengeData hcd " +
            "WHERE hcd.challengeId = :challengeId " +
            "AND hcd.intervalType = :intervalType " +
            "AND hcd.eventDate <= :targetDate " +
            "ORDER BY hcd.eventDate ASC")
    List<HistoryChallengeData> findByIntervalTypeAndDate(@Param("challengeId") Integer challengeId, @Param("intervalType") String intervalType, @Param("targetDate") LocalDateTime targetDate);

    /**
     * 특정 챌린지의 가장 최신 주가 데이터를 한 건 조회합니다.
     * @param challengeId 챌린지 ID
     * @return 가장 최신 주가 데이터
     */
    HistoryChallengeData findTopByChallengeIdOrderByEventDateDesc(Integer challengeId);

}
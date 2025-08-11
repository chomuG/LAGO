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

    /**
     * 특정 챌린지의 주가 데이터를 조회합니다.
     * @param challengeId 챌린지 ID
     * @param intervalType 간격
     * @param targetDate 현재 일시
     * @return 역사챌린지 주가 데이터 목록
     */
    @Query("SELECT hcd FROM HistoryChallengeData hcd " +
            "WHERE hcd.challengeId = :challengeId ")
    List<HistoryChallengeData> findByIntervalTypeAndDate(@Param("challengeId") Integer challengeId, @Param("intervalType") String intervalType, @Param("targetDate") LocalDateTime targetDate);

    /**
     * 특정 챌린지의 가장 최신 주가 데이터를 조회합니다.
     * @param challengeId 챌린지 ID
     * @return 역사챌린지 최신 주가 데이터
     */
    @Query("SELECT hcd FROM HistoryChallengeData hcd " +
            "WHERE hcd.challengeId = :challengeId " )
    HistoryChallengeData findTopByChallengeIdOrderByEventDateDesc(@Param("challengeId") Integer challengeId, @Param("targetDate") LocalDateTime targetDate);


    HistoryChallengeData findTopByChallengeIdOrderByDateDesc(Integer challengeId);

    /**
     * 특정 챌린지의 지정된 가상 시간 범위 내의 모든 주가 데이터를 조회합니다.
     * @param challengeId 챌린지 ID
     * @param virtualStartDate 조회할 가상 시작 시간
     * @param virtualEndDate 조회할 가상 종료 시간
     * @return 해당 시간 범위의 주가 데이터 목록 (시간순)
     */
    List<HistoryChallengeData> findByChallengeIdAndDateBetweenOrderByDateAsc(
        Integer challengeId, 
        LocalDateTime virtualStartDate, 
        LocalDateTime virtualEndDate
    );

    /**
     * 특정 챌린지의 지정된 과거 내의 모든 주가 데이터를 조회합니다.
     * @param challengeId 챌린지 ID
     * @param virtualEndDate 조회할 과거 종료 시간
     * @return 해당 시간 범위의 주가 데이터 목록 (시간순)
     */
    List<HistoryChallengeData> findByChallengeIdAndDateLessThanOrderByDateAsc(
            Integer challengeId,
            LocalDateTime virtualEndDate
    );

    @Query(value = "SELECT " +
            "time_bucket(CAST(:interval AS INTERVAL), hcd.date) AS bucket, " +
            "FIRST(hcd.open_price, hcd.date) AS open, " +
            "MAX(hcd.high_price) AS high, " +
            "MIN(hcd.low_price) AS low, " +
            "LAST(hcd.close_price, hcd.date) AS close, " +
            "SUM(hcd.volume) AS volume, " +
            "(LAST(hcd.close_price, hcd.date) - FIRST(hcd.open_price, hcd.date)) AS change_price, " +
            "CASE WHEN FIRST(hcd.open_price, hcd.date) = 0 THEN 0.0 " +
                "ELSE ((LAST(hcd.close_price, hcd.date) - FIRST(hcd.open_price, hcd.date)) / FIRST(hcd.open_price, hcd.date)) * 100.0 " +
            "END AS change_rate " +
            "FROM \"HISTORY_CHALLENGE_DATA\" hcd " +
            "WHERE hcd.challenge_id = :challengeId AND hcd.date < :virtualCurrentTime " +
            "GROUP BY bucket " +
            "ORDER BY bucket", nativeQuery = true)
    List<Object[]> findAggregatedByChallengeIdAndDate(
            @Param("challengeId") Integer challengeId,
            @Param("virtualCurrentTime") LocalDateTime virtualCurrentTime,
            @Param("interval") String interval
    );
}

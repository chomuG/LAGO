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
     * 특정 챌린지의 가장 최신 주가 데이터를 조회합니다.
     * @param challengeId 챌린지 ID
     * @return 역사챌린지 최신 주가 데이터
     */
    @Query("SELECT hcd FROM HistoryChallengeData hcd " +
            "WHERE hcd.challengeId = :challengeId " +
            "AND hcd.date <= :targetDate " +
            "ORDER BY hcd.date LIMIT 1 " )
    HistoryChallengeData findTopByChallengeIdOrderByEventDateDesc(@Param("challengeId") Integer challengeId, @Param("targetDate") LocalDateTime targetDate);

    /**
     * 특정 챌린지의 지정된 과거 내의 모든 주가 데이터를 조회합니다.
     * @param challengeId 챌린지 ID
     * @param virtualCurrentTime 과거 시간
     * @param interval 조회 간격
     * @return 해당 시간 범위의 주가 데이터 목록 (시간순)
     */
    @Query(value = "SELECT \n" +
            "    ROW_NUMBER() OVER (ORDER BY bucket) AS row_id, " +
            "    sub.* " +
            "FROM (" +
                "SELECT " +
                    "time_bucket(CAST(:interval AS INTERVAL), hcd.date) AS bucket, " +
                    "FIRST(hcd.open_price, hcd.date) AS open, " +
                    "MAX(hcd.high_price) AS high, " +
                    "MIN(hcd.low_price) AS low, " +
                    "LAST(hcd.close_price, hcd.date) AS close, " +
                    "SUM(hcd.volume) AS volume, " +
                    "(LAST(hcd.close_price, hcd.date) - FIRST(hcd.open_price, hcd.date)) AS change_price " +
                "FROM \"HISTORY_CHALLENGE_DATA\" hcd " +
                "WHERE hcd.challenge_id = :challengeId AND hcd.date <= :virtualCurrentTime " +
                    "AND hcd.date::time >= '09:00:00' " +
                    "AND hcd.date::time <= '15:00:00' " +
                "GROUP BY bucket " +
                "ORDER BY bucket " +
            ") sub", nativeQuery = true)
    List<Object[]> findAggregatedByChallengeIdAndDate(
            @Param("challengeId") Integer challengeId,
            @Param("virtualCurrentTime") LocalDateTime virtualCurrentTime,
            @Param("interval") String interval
    );
}

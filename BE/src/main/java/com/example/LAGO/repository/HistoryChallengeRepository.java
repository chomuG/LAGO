package com.example.LAGO.repository;

import com.example.LAGO.domain.HistoryChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface HistoryChallengeRepository extends JpaRepository<HistoryChallenge, Long> {
    /**
     * 특정 날짜에 진행 중인 역사챌린지를 조회합니다.
     *
     * @param targetDate 조회할 날짜
     * @return 역사챌린지 정보
     */
    @Query("SELECT hc FROM HistoryChallenge hc WHERE :targetDate BETWEEN hc.startDate AND hc.endDate")
    HistoryChallenge findByDate(@Param("targetDate") LocalDate targetDate);
}
package com.example.LAGO.repository;

import com.example.LAGO.domain.StockInfo;
import com.example.LAGO.domain.StockMinute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMinuteRepository extends JpaRepository<StockMinute, Integer> {
       // 구간 조회 (stock_info_id, 시작일~종료일)
       List<StockMinute> findByStockInfoAndDateBetweenOrderByDateAsc(
                     StockInfo stockInfoId,
                     LocalDateTime start,
                     LocalDateTime end
       );

       // 특정 종목의, 특정 시간 분 데이터(최신 시간) 한 건 조회
       StockMinute findTopByStockInfoAndDateOrderByDateDesc(StockInfo stockInfoId, LocalDateTime date);
}
package com.example.LAGO.repository;

<<<<<<< HEAD
import com.example.LAGO.domain.StockMinute;
=======
import com.example.LAGO.domain.StockInfo;
import com.example.LAGO.domain.StockMinute;
import org.springframework.data.domain.Pageable;
>>>>>>> origin/backend-dev
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

<<<<<<< HEAD
/**
 * 주식 분단위 데이터 Repository
 * 지침서 명세: 연동된 EC2 DB STOCK_MINUTE 테이블 연동
 * 
 * 실시간 주가 분석 및 기술적 지표 계산을 위한 분단위 데이터 처리
 * Java 21 Virtual Thread를 활용한 대용량 데이터 조회 최적화
 * 
 * @author D203팀 백엔드 개발자
 * @since 2025-08-06
 */
@Repository
public interface StockMinuteRepository extends JpaRepository<StockMinute, Integer> {

    /**
     * 종목 정보 ID와 특정 시간으로 분단위 데이터 조회
     * 
     * @param stockInfoId 종목 정보 ID
     * @param dateTime 거래 일시
     * @return 분단위 데이터
     */
    Optional<StockMinute> findByStockInfoIdAndDateTime(Integer stockInfoId, LocalDateTime dateTime);

    /**
     * 종목 정보 ID로 최근 N분 데이터 조회 (최신순)
     * 
     * @param stockInfoId 종목 정보 ID
     * @param limit 조회할 분 수
     * @return 분단위 데이터 리스트 (최신순)
     */
    @Query("SELECT sm FROM StockMinute sm WHERE sm.stockInfoId = :stockInfoId " +
           "ORDER BY sm.dateTime DESC LIMIT :limit")
    List<StockMinute> findByStockInfoIdOrderByDateTimeDescLimit(@Param("stockInfoId") Integer stockInfoId, 
                                                               @Param("limit") int limit);

    /**
     * 종목 정보 ID로 특정 기간 분단위 데이터 조회
     * 
     * @param stockInfoId 종목 정보 ID
     * @param startDateTime 시작 일시
     * @param endDateTime 종료 일시
     * @return 분단위 데이터 리스트 (시간순)
     */
    List<StockMinute> findByStockInfoIdAndDateTimeBetweenOrderByDateTimeAsc(Integer stockInfoId, 
                                                                           LocalDateTime startDateTime, 
                                                                           LocalDateTime endDateTime);

    /**
     * 종목 정보 ID로 최신 분단위 데이터 조회
     * 
     * @param stockInfoId 종목 정보 ID
     * @return 최신 분단위 데이터
     */
    Optional<StockMinute> findTopByStockInfoIdOrderByDateTimeDesc(Integer stockInfoId);

    /**
     * 특정 시간의 모든 종목 분단위 데이터 조회
     * 
     * @param dateTime 거래 일시
     * @return 분단위 데이터 리스트
     */
    List<StockMinute> findByDateTimeOrderByStockInfoIdAsc(LocalDateTime dateTime);

    /**
     * 거래량 기준 상위 종목 조회 (특정 시간)
     * 
     * @param dateTime 거래 일시
     * @param limit 조회 개수
     * @return 거래량 상위 종목 리스트
     */
    @Query("SELECT sm FROM StockMinute sm WHERE sm.dateTime = :dateTime " +
           "ORDER BY sm.volume DESC LIMIT :limit")
    List<StockMinute> findTopByVolumeAtDateTime(@Param("dateTime") LocalDateTime dateTime, 
                                               @Param("limit") int limit);

    /**
     * 등락률 기준 상승 종목 조회 (특정 시간)
     * 
     * @param dateTime 거래 일시
     * @param limit 조회 개수
     * @return 등락률 상위 종목 리스트
     */
    @Query("SELECT sm FROM StockMinute sm WHERE sm.dateTime = :dateTime AND sm.fluctuationRate > 0 " +
           "ORDER BY sm.fluctuationRate DESC LIMIT :limit")
    List<StockMinute> findTopGainersAtDateTime(@Param("dateTime") LocalDateTime dateTime, 
                                              @Param("limit") int limit);

    /**
     * 등락률 기준 하락 종목 조회 (특정 시간)
     * 
     * @param dateTime 거래 일시
     * @param limit 조회 개수
     * @return 등락률 하위 종목 리스트
     */
    @Query("SELECT sm FROM StockMinute sm WHERE sm.dateTime = :dateTime AND sm.fluctuationRate < 0 " +
           "ORDER BY sm.fluctuationRate ASC LIMIT :limit")
    List<StockMinute> findTopLosersAtDateTime(@Param("dateTime") LocalDateTime dateTime, 
                                             @Param("limit") int limit);

    /**
     * 종목별 평균 거래량 계산 (최근 N분 기준)
     * 
     * @param stockInfoId 종목 정보 ID
     * @param startDateTime 시작 일시
     * @return 평균 거래량
     */
    @Query("SELECT AVG(sm.volume) FROM StockMinute sm WHERE sm.stockInfoId = :stockInfoId " +
           "AND sm.dateTime >= :startDateTime")
    Double getAverageVolumeByPeriod(@Param("stockInfoId") Integer stockInfoId, 
                                   @Param("startDateTime") LocalDateTime startDateTime);

    /**
     * 종목별 가격 변동성 계산 (표준편차 기반)
     * 
     * @param stockInfoId 종목 정보 ID
     * @param startDateTime 시작 일시
     * @param endDateTime 종료 일시
     * @return 가격 변동성
     */
    @Query("SELECT STDDEV(sm.closePrice) FROM StockMinute sm WHERE sm.stockInfoId = :stockInfoId " +
           "AND sm.dateTime BETWEEN :startDateTime AND :endDateTime")
    Double getPriceVolatility(@Param("stockInfoId") Integer stockInfoId,
                             @Param("startDateTime") LocalDateTime startDateTime,
                             @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 거래량 급증 종목 조회
     * 
     * @param volumeThreshold 거래량 임계값
     * @param dateTime 조회 시간
     * @return 거래량 급증 종목 리스트
     */
    @Query("SELECT sm FROM StockMinute sm WHERE sm.dateTime = :dateTime " +
           "AND sm.volume > :volumeThreshold ORDER BY sm.volume DESC")
    List<StockMinute> findVolumeSpikes(@Param("volumeThreshold") Long volumeThreshold,
                                      @Param("dateTime") LocalDateTime dateTime);

    /**
     * 기술적 분석용 OHLCV 데이터 조회 (최근 N분)
     * 실시간 차트 및 지표 계산에 사용
     * 
     * @param stockInfoId 종목 정보 ID
     * @param startDateTime 시작 일시
     * @return OHLCV 데이터 리스트 (시간순)
     */
    @Query("SELECT sm FROM StockMinute sm WHERE sm.stockInfoId = :stockInfoId " +
           "AND sm.dateTime >= :startDateTime ORDER BY sm.dateTime ASC")
    List<StockMinute> findOHLCVDataForAnalysis(@Param("stockInfoId") Integer stockInfoId,
                                              @Param("startDateTime") LocalDateTime startDateTime);

    /**
     * 시간대별 거래 활성도 조회
     * 
     * @param startTime 시작 시간 (HH:mm 형식)
     * @param endTime 종료 시간 (HH:mm 형식)
     * @param dateTime 기준 날짜
     * @return 해당 시간대 거래 데이터
     */
    @Query("SELECT sm FROM StockMinute sm WHERE DATE(sm.dateTime) = DATE(:dateTime) " +
           "AND TIME(sm.dateTime) BETWEEN :startTime AND :endTime " +
           "ORDER BY sm.volume DESC")
    List<StockMinute> findByTimeRange(@Param("startTime") String startTime,
                                     @Param("endTime") String endTime,
                                     @Param("dateTime") LocalDateTime dateTime);
}
=======
public interface StockMinuteRepository extends JpaRepository<StockMinute, Integer> {
    // 구간 조회 (stock_info_id, 시작일~종료일)
    List<StockMinute> findByStockInfoAndDateBetweenOrderByDateAsc(
            StockInfo stockInfoId,
            LocalDateTime start,
            LocalDateTime end
    );

    // 특정 종목의, 특정 시간 분 데이터(최신 시간) 한 건 조회
    StockMinute findTopByStockInfoAndDateOrderByDateDesc(StockInfo stockInfoId, LocalDateTime date);


}
>>>>>>> origin/backend-dev

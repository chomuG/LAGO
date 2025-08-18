package com.lago.app.domain.repository

import com.lago.app.domain.entity.*
import com.lago.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface ChartRepository {
    
    /**
     * 주식 기본 정보 조회
     */
    suspend fun getStockInfo(stockCode: String): Flow<Resource<ChartStockInfo>>
    
    /**
     * 차트 데이터 조회 (기존)
     */
    suspend fun getCandlestickData(
        stockCode: String,
        timeFrame: String,
        period: Int = 100
    ): Flow<Resource<List<CandlestickData>>>
    
    /**
     * 인터벌 차트 데이터 조회 (새로운 API)
     */
    suspend fun getIntervalChartData(
        stockCode: String,
        interval: String, // "MINUTE1", "MINUTE5", "MINUTE15", "MINUTE30", "HOUR1", "DAY"
        fromDateTime: String, // KST: "2024-08-13T09:00:00"
        toDateTime: String // KST: "2024-08-15T15:30:00"
    ): Flow<Resource<List<CandlestickData>>>
    
    /**
     * 거래량 데이터 조회
     */
    suspend fun getVolumeData(
        stockCode: String,
        timeFrame: String,
        period: Int = 100
    ): Flow<Resource<List<VolumeData>>>
    
    /**
     * 기술지표 데이터 조회
     */
    suspend fun getIndicators(
        stockCode: String,
        indicators: List<String>,
        timeFrame: String,
        period: Int = 100
    ): Flow<Resource<ChartIndicatorData>>
    
    /**
     * 사용자 보유 종목 조회
     */
    suspend fun getUserHoldings(): Flow<Resource<List<HoldingItem>>>
    
    /**
     * 과거 캔들스틱 데이터 조회 (무한 히스토리용)
     */
    suspend fun getHistoricalCandlestickData(
        stockCode: String,
        timeFrame: String,
        beforeTime: Long? = null,  // 이 시간 이전 데이터
        limit: Int = 50
    ): Flow<Resource<List<CandlestickData>>>
    
    /**
     * 거래 내역 조회
     */
    suspend fun getTradingHistory(
        stockCode: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Flow<Resource<TradingHistoryPage>>
    
    /**
     * 관심종목 추가
     */
    suspend fun addToFavorites(stockCode: String): Flow<Resource<Boolean>>
    
    /**
     * 관심종목 제거
     */
    suspend fun removeFromFavorites(stockCode: String): Flow<Resource<Boolean>>
    
    /**
     * 관심종목 목록 조회
     */
    suspend fun getFavorites(): Flow<Resource<List<String>>>
    
    /**
     * 관심종목 여부 확인
     */
    suspend fun isFavorite(stockCode: String): Flow<Resource<Boolean>>
    
    /**
     * 차트 패턴 분석
     */
    suspend fun analyzeChartPattern(
        request: com.lago.app.data.remote.dto.PatternAnalysisRequest
    ): Flow<Resource<List<com.lago.app.data.remote.dto.PatternAnalysisResponse>>>

    // ===== 역사챌린지 관련 메서드 =====

    /**
     * 역사챌린지 조회 (단일 챌린지)
     */
    suspend fun getHistoryChallenge(): Flow<Resource<com.lago.app.data.remote.dto.HistoryChallengeResponse>>

    /**
     * 역사챌린지 차트 데이터 조회 (현재 시간 기준 과거 기간)
     */
    suspend fun getHistoryChallengeChart(
        challengeId: Int,
        interval: String,
        pastMinutes: Int? = null,  // 과거 몇 분 (분봉용)
        pastDays: Int? = null      // 과거 몇 일 (일봉용) 
    ): Flow<Resource<List<CandlestickData>>>

    /**
     * 역사챌린지 차트 무한 히스토리 데이터 조회 (특정 시간 이전 데이터)
     */
    suspend fun getHistoryChallengeHistoricalData(
        challengeId: Int,
        interval: String,
        beforeDateTime: String,  // 이 시간 이전 데이터
        limit: Int = 50
    ): Flow<Resource<List<CandlestickData>>>

    /**
     * 역사챌린지 뉴스 목록 조회
     */
    suspend fun getHistoryChallengeNews(
        challengeId: Int,
        pastDateTime: String
    ): Flow<Resource<List<com.lago.app.data.remote.dto.HistoryChallengeNewsResponse>>>

}
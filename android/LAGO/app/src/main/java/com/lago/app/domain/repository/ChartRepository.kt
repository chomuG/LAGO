package com.lago.app.domain.repository

import com.lago.app.domain.entity.*
import com.lago.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface ChartRepository {
    
    /**
     * 주식 기본 정보 조회
     */
    suspend fun getStockInfo(stockCode: String): Flow<Resource<StockInfo>>
    
    /**
     * 차트 데이터 조회
     */
    suspend fun getCandlestickData(
        stockCode: String,
        timeFrame: String,
        period: Int = 100
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
        stockCode: String,
        timeFrame: String,
        startTime: String? = null,
        endTime: String? = null
    ): Flow<Resource<PatternAnalysisResult>>
}
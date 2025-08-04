package com.lago.app.data.remote.api

import com.lago.app.data.remote.dto.*
import retrofit2.http.*

interface ChartApiService {
    
    /**
     * 주식 기본 정보 조회
     */
    @GET("api/stock/{stockCode}")
    suspend fun getStockInfo(
        @Path("stockCode") stockCode: String
    ): StockInfoResponse

    /**
     * 차트 데이터 조회 (캔들스틱)
     */
    @GET("api/stock/{stockCode}/chart")
    suspend fun getCandlestickData(
        @Path("stockCode") stockCode: String,
        @Query("timeFrame") timeFrame: String,
        @Query("period") period: Int = 100
    ): CandlestickResponse

    /**
     * 거래량 데이터 조회
     */
    @GET("api/stock/{stockCode}/volume")
    suspend fun getVolumeData(
        @Path("stockCode") stockCode: String,
        @Query("timeFrame") timeFrame: String,
        @Query("period") period: Int = 100
    ): VolumeResponse

    /**
     * 기술지표 데이터 조회
     */
    @GET("api/stock/{stockCode}/indicators")
    suspend fun getIndicators(
        @Path("stockCode") stockCode: String,
        @Query("indicators") indicators: String, // "sma5,sma20,rsi,macd"
        @Query("timeFrame") timeFrame: String,
        @Query("period") period: Int = 100
    ): IndicatorsResponse

    /**
     * 사용자 보유 종목 조회
     */
    @GET("api/account/holdings")
    suspend fun getUserHoldings(
        @Header("Authorization") token: String
    ): HoldingsResponse

    /**
     * 거래 내역 조회
     */
    @GET("api/account/transactions")
    suspend fun getTradingHistory(
        @Header("Authorization") token: String,
        @Query("stockCode") stockCode: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): TradingHistoryResponse

    /**
     * 관심종목 추가/제거
     */
    @POST("api/account/favorites/{stockCode}")
    suspend fun addToFavorites(
        @Header("Authorization") token: String,
        @Path("stockCode") stockCode: String
    ): BaseResponse

    @DELETE("api/account/favorites/{stockCode}")
    suspend fun removeFromFavorites(
        @Header("Authorization") token: String,
        @Path("stockCode") stockCode: String
    ): BaseResponse

    /**
     * 관심종목 목록 조회
     */
    @GET("api/account/favorites")
    suspend fun getFavorites(
        @Header("Authorization") token: String
    ): FavoritesResponse
}
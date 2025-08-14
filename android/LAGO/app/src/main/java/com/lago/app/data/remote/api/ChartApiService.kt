package com.lago.app.data.remote.api

import com.lago.app.data.remote.dto.*
import retrofit2.http.*

interface ChartApiService {

    /**
     * 일별 주식 정보 조회
     */
    @GET("api/charts/stock-day/{stockId}")
    suspend fun getStockDayData(
        @Path("stockId") stockId: Int,
        @Query("start") startDate: String,
        @Query("end") endDate: String
    ): List<StockDayDto>

    /**
     * 분봉 주식 정보 조회
     */
    @GET("api/charts/stock-minute/{stockId}")
    suspend fun getStockMinuteData(
        @Path("stockId") stockId: Int,
        @Query("start") startDateTime: String,
        @Query("end") endDateTime: String
    ): List<StockMinuteDto>

    /**
     * 월별 주식 정보 조회
     */
    @GET("api/charts/stock-month/{stockId}")
    suspend fun getStockMonthData(
        @Path("stockId") stockId: Int,
        @Query("start") startMonth: Int,
        @Query("end") endMonth: Int
    ): List<StockMonthDto>

    /**
     * 년도별 주식 정보 조회
     */
    @GET("api/charts/stock-year/{stockId}")
    suspend fun getStockYearData(
        @Path("stockId") stockId: Int,
        @Query("start") startYear: Int,
        @Query("end") endYear: Int
    ): List<StockYearDto>

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
     * 관심종목 목록 조회 (기존 API)
     */
    @GET("api/account/favorites")
    suspend fun getFavorites(
        @Header("Authorization") token: String
    ): FavoritesResponse

    /**
     * 주식 목록 조회 (전체) - 임시로 원래대로 복원
     */
    @GET("api/stocks/info")
    suspend fun getStockList(
        @Query("category") category: String? = null, // "kospi", "kosdaq", "favorites", "trending"
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "code", // "code", "name", "price", "changeRate"
        @Query("search") search: String? = null
    ): List<SimpleStockDto>

    /**
     * 종목 코드로 주식 종목 조회
     */
    @GET("api/stocks/info/{code}")
    suspend fun getStockInfo(
        @Path("code") stockCode: String
    ): SimpleStockDto

    /**
     * 인기 주식 목록 조회 - 원래대로 복원
     */
    @GET("api/stocks/trending")
    suspend fun getTrendingStocks(
        @Query("limit") limit: Int = 20
    ): List<SimpleStockDto>

    /**
     * 주식 검색 - 원래대로 복원
     */
    @GET("api/stocks/search")
    suspend fun searchStocks(
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): List<SimpleStockDto>

    /**
     * 차트 패턴 분석
     */
    @POST("api/stock/{stockCode}/pattern-analysis")
    suspend fun analyzeChartPattern(
        @Header("Authorization") token: String,
        @Path("stockCode") stockCode: String,
        @Body request: PatternAnalysisRequest
    ): PatternAnalysisResponse

    // =================================================
    // 모의투자 관련 API (실제 DB 스키마 기반)
    // =================================================

    /**
     * 주식 매수 주문 (MOCK_TRADE 테이블에 INSERT)
     */
    @POST("api/mock-trade/buy")
    suspend fun buyStock(
        @Header("Authorization") token: String,
        @Body request: MockTradeRequest
    ): BaseResponse<MockTradeResponse>

    /**
     * 주식 매도 주문 (MOCK_TRADE 테이블에 INSERT)
     */
    @POST("api/mock-trade/sell")
    suspend fun sellStock(
        @Header("Authorization") token: String,
        @Body request: MockTradeRequest
    ): BaseResponse<MockTradeResponse>

    /**
     * 계좌 잔고 정보 조회 (ACCOUNTS 테이블)
     */
    @GET("api/accounts/balance")
    suspend fun getAccountBalance(
        @Header("Authorization") token: String
    ): AccountBalanceResponse

    /**
     * 보유 주식 현황 조회 (STOCK_HOLDING + STOCK_INFO + TICKS 조인)
     */
    @GET("api/accounts/holdings")
    suspend fun getStockHoldings(
        @Header("Authorization") token: String
    ): StockHoldingsResponse

    /**
     * 거래 내역 조회 (MOCK_TRADE 테이블 페이징)
     */
    @GET("api/accounts/transactions")
    suspend fun getMockTradeHistory(
        @Header("Authorization") token: String,
        @Query("stockCode") stockCode: String? = null,
        @Query("buySell") buySell: String? = null, // "BUY" or "SELL"
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): MockTradeHistoryResponse

    /**
     * 주식 목록 조회 (STOCK_INFO + TICKS 조인, 실시간 가격 포함)
     */
    @GET("api/stocks/list")
    suspend fun getStockListWithRealtime(
        @Query("market") market: String? = null, // "KOSPI", "KOSDAQ"
        @Query("category") category: String? = null, // "trending", "volume"
        @Query("sort") sort: String = "code",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): StockListResponse

    /**
     * 개별 주식 정보 조회 (실시간 가격 포함)
     */
    @GET("api/stocks/{stockCode}")
    suspend fun getStockInfoWithRealtime(
        @Path("stockCode") stockCode: String
    ): StockInfoResponse

    /**
     * 주식 검색 (종목명/종목코드 검색)
     */
    @GET("api/stocks/search")
    suspend fun searchStocksWithRealtime(
        @Query("query") query: String,
        @Query("market") market: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): StockListResponse

    /**
     * 관심종목 추가 (INTEREST 테이블에 INSERT)
     */
    @POST("api/stocks/favorites")
    suspend fun addToFavorites(
        @Header("Authorization") token: String,
        @Body request: FavoriteStockRequest
    ): SimpleBaseResponse

    /**
     * 관심종목 삭제 (INTEREST 테이블에서 DELETE)
     */
    @DELETE("api/stocks/favorites/{stockCode}")
    suspend fun removeFromFavorites(
        @Header("Authorization") token: String,
        @Path("stockCode") stockCode: String
    ): SimpleBaseResponse

    /**
     * 관심종목 목록 조회 (INTEREST + STOCK_INFO + TICKS 조인)
     */
    @GET("api/stocks/favorites")
    suspend fun getFavoriteStocks(
        @Header("Authorization") token: String
    ): FavoriteStocksResponse

    /**
     * 계좌 초기화 (신규 가입시 ACCOUNTS 테이블 생성)
     */
    @POST("api/accounts/initialize")
    suspend fun initializeAccount(
        @Header("Authorization") token: String,
        @Body request: InitializeAccountRequest
    ): AccountBalanceResponse

    /**
     * 모의투자 리셋 (계좌 초기화)
     */
    @POST("api/accounts/reset")
    suspend fun resetAccount(
        @Header("Authorization") token: String
    ): AccountResetResponse

    /**
     * 종목 코드로 일봉 데이터 조회 (장 마감 시 종가 조회용)
     */
    @GET("api/stocks/day/{code}")
    suspend fun getStockDayByCode(
        @Path("code") stockCode: String,
        @Query("start") startDate: String,
        @Query("end") endDate: String
    ): List<StockDayDto>
}
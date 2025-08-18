package com.lago.app.data.repository

import com.lago.app.data.local.prefs.UserPreferences
import com.lago.app.data.cache.ChartMemoryCache
import com.lago.app.data.mapper.ChartDataMapper.toDomain
import com.lago.app.data.mapper.ChartDataMapper.toHoldingItemList
import com.lago.app.data.mapper.ChartDataMapper.toLineDataList
import com.lago.app.data.mapper.ChartDataMapper.toTradingItemList
import com.lago.app.data.mapper.ChartDataMapper.toVolumeDataList
import com.lago.app.data.remote.api.ChartApiService
import com.lago.app.data.remote.dto.FavoriteStockRequest
import com.lago.app.domain.entity.*
import com.lago.app.domain.repository.ChartRepository
import com.lago.app.util.Resource
import com.lago.app.util.StockCodeMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import com.lago.app.presentation.ui.chart.v5.ChartTimeManager
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChartRepositoryImpl @Inject constructor(
    private val apiService: ChartApiService,
    private val userPreferences: UserPreferences,
    private val memoryCache: ChartMemoryCache
) : ChartRepository {

    override suspend fun getStockInfo(stockCode: String): Flow<Resource<ChartStockInfo>> = flow {
        try {
            emit(Resource.Loading())
            
            // 1. 메모리 캐시에서 먼저 확인
            val cachedData = memoryCache.getChartStockInfo(stockCode)
            if (cachedData != null) {
                emit(Resource.Success(cachedData))
                return@flow
            }
            
            // 2. 주식 코드를 주식 ID로 변환
            val stockId = StockCodeMapper.getStockId(stockCode)
            if (stockId == null) {
                emit(Resource.Error("Unknown stock code: $stockCode"))
                return@flow
            }
            
            // 3. 최근 일별 데이터를 가져와서 주식 정보 추출 (임시 방법)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val endDate = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            val startDate = dateFormat.format(calendar.time)
            
            val dayData = apiService.getStockDayData(stockId, startDate, endDate)
            if (dayData.isNotEmpty()) {
                val latestData = dayData.last()
                val stockInfo = ChartStockInfo(
                    code = stockCode,
                    name = getStockName(stockCode),
                    currentPrice = latestData.closePrice.toFloat(),
                    priceChange = (latestData.closePrice - latestData.openPrice).toFloat(),
                    priceChangePercent = latestData.fluctuationRate,
                    previousDay = null // 웹소켓으로 업데이트
                )
                
                // 4. 메모리 캐시에 저장 (5분 TTL)
                memoryCache.putChartStockInfo(stockCode, stockInfo, 5)
                
                emit(Resource.Success(stockInfo))
            } else {
                emit(Resource.Error("No stock data available"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }
    
    private fun getStockName(stockCode: String): String {
        // TODO: 실제 API에서 종목명 조회하도록 수정
        return ""
    }

    override suspend fun getCandlestickData(
        stockCode: String,
        timeFrame: String,
        period: Int
    ): Flow<Resource<List<CandlestickData>>> = flow {
        try {
            emit(Resource.Loading())
            
            // 1. 메모리 캐시에서 먼저 확인
            val cachedData = memoryCache.getCandlestickData(stockCode, timeFrame)
            if (cachedData != null) {
                emit(Resource.Success(cachedData))
                return@flow
            }
            
            // 2. 주식 코드를 주식 ID로 변환
            val stockId = StockCodeMapper.getStockId(stockCode)
            if (stockId == null) {
                emit(Resource.Error("Unknown stock code: $stockCode"))
                return@flow
            }
            
            // 3. 타임프레임에 따라 적절한 API 호출
            val candlestickData = when (timeFrame) {
                "D" -> {
                    // 일별 데이터
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val endDate = dateFormat.format(calendar.time)
                    calendar.add(Calendar.DAY_OF_MONTH, -period)
                    val startDate = dateFormat.format(calendar.time)
                    val response = apiService.getStockDayData(stockId, startDate, endDate)
                    response.map { dto ->
                        CandlestickData(
                            time = ChartTimeManager.parseDate(dto.date),
                            open = dto.openPrice.toFloat(),
                            high = dto.highPrice.toFloat(),
                            low = dto.lowPrice.toFloat(),
                            close = dto.closePrice.toFloat(),
                            volume = dto.volume.toLong()
                        )
                    }
                }
                "1", "3", "5", "15", "30", "60" -> {
                    // 분봉 데이터
                    val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val endDateTime = dateTimeFormat.format(calendar.time)
                    calendar.add(Calendar.HOUR_OF_DAY, -period)
                    val startDateTime = dateTimeFormat.format(calendar.time)
                    val response = apiService.getStockMinuteData(stockId, startDateTime, endDateTime)
                    response.map { dto ->
                        CandlestickData(
                            time = ChartTimeManager.parseDateTime(dto.date),
                            open = dto.openPrice.toFloat(),
                            high = dto.highPrice.toFloat(),
                            low = dto.lowPrice.toFloat(),
                            close = dto.closePrice.toFloat(),
                            volume = dto.volume.toLong()
                        )
                    }
                }
                "M" -> {
                    // 월별 데이터
                    val calendar = Calendar.getInstance()
                    val endMonth = calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)
                    calendar.add(Calendar.MONTH, -period)
                    val startMonth = calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)
                    val response = apiService.getStockMonthData(stockId, startMonth, endMonth)
                    response.map { dto ->
                        CandlestickData(
                            time = ChartTimeManager.parseMonthDate(dto.date),
                            open = dto.openPrice.toFloat(),
                            high = dto.highPrice.toFloat(),
                            low = dto.lowPrice.toFloat(),
                            close = dto.closePrice.toFloat(),
                            volume = dto.volume.toLong()
                        )
                    }
                }
                else -> {
                    emptyList()
                }
            }
            
            if (candlestickData.isNotEmpty()) {
                // 4. 메모리 캐시에 저장 (15분 TTL)
                memoryCache.putCandlestickData(stockCode, timeFrame, candlestickData, 15)
                
                emit(Resource.Success(candlestickData))
            } else {
                emit(Resource.Error("No candlestick data available"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }
    
    
    override suspend fun getIntervalChartData(
        stockCode: String,
        interval: String,
        fromDateTime: String,
        toDateTime: String
    ): Flow<Resource<List<CandlestickData>>> = flow {
        try {
            emit(Resource.Loading())
            
            android.util.Log.d("ChartRepositoryImpl", "🔥 모의투자 인터벌 API 호출 시작")
            android.util.Log.d("ChartRepositoryImpl", "🔥 파라미터: stockCode=$stockCode, interval=$interval")
            android.util.Log.d("ChartRepositoryImpl", "🔥 시간범위: $fromDateTime ~ $toDateTime")
            android.util.Log.d("ChartRepositoryImpl", "🔥 호출 URL: GET /api/stocks/$stockCode?interval=$interval&fromDateTime=$fromDateTime&toDateTime=$toDateTime")
            
            // 새로운 인터벌 API 호출
            val response = apiService.getIntervalChartData(stockCode, interval, fromDateTime, toDateTime)
            
            android.util.Log.d("ChartRepositoryImpl", "🔥 API 응답 받음: ${response.size}개 데이터")
            if (response.isNotEmpty()) {
                val firstDto = response.first()
                val lastDto = response.last()
                android.util.Log.d("ChartRepositoryImpl", "🔥 첫 데이터: bucket=${firstDto.bucket}, close=${firstDto.closePrice}")
                android.util.Log.d("ChartRepositoryImpl", "🔥 마지막 데이터: bucket=${lastDto.bucket}, close=${lastDto.closePrice}")
            }
            
            // IntervalChartDataDto를 CandlestickData로 변환
            val candlestickData = response.map { dto ->
                // bucket 필드를 epoch seconds로 변환 (이미 버킷 시작 시각임)
                val bucketEpochSec = ChartTimeManager.parseDateTime(dto.bucket)
                
                android.util.Log.v("ChartRepositoryImpl", "🔥 변환: ${dto.bucket} → $bucketEpochSec (${java.util.Date(bucketEpochSec * 1000)})")
                
                CandlestickData(
                    time = bucketEpochSec, // 이미 정확한 버킷 시작 시각
                    open = dto.openPrice.toFloat(),
                    high = dto.highPrice.toFloat(),
                    low = dto.lowPrice.toFloat(),
                    close = dto.closePrice.toFloat(),
                    volume = dto.volume
                )
            }
            
            android.util.Log.d("ChartRepositoryImpl", "🔥 최종 변환 완료: ${candlestickData.size}개 캔들")
            if (candlestickData.isNotEmpty()) {
                val firstCandle = candlestickData.first()
                val lastCandle = candlestickData.last()
                android.util.Log.d("ChartRepositoryImpl", "🔥 첫 캔들: time=${firstCandle.time} (${java.util.Date(firstCandle.time * 1000)}), close=${firstCandle.close}")
                android.util.Log.d("ChartRepositoryImpl", "🔥 마지막 캔들: time=${lastCandle.time} (${java.util.Date(lastCandle.time * 1000)}), close=${lastCandle.close}")
            }
            
            emit(Resource.Success(candlestickData))
            
        } catch (e: HttpException) {
            android.util.Log.e("ChartRepositoryImpl", "🚨 HTTP 에러: ${e.code()} - ${e.message()}")
            android.util.Log.e("ChartRepositoryImpl", "🚨 HTTP 응답 본문: ${e.response()?.errorBody()?.string()}")
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            android.util.Log.e("ChartRepositoryImpl", "🚨 연결 에러: ${e.localizedMessage}")
            emit(Resource.Error("Connection error: ${e.localizedMessage}"))
        } catch (e: Exception) {
            android.util.Log.e("ChartRepositoryImpl", "🚨 예외 발생: ${e.localizedMessage}")
            android.util.Log.e("ChartRepositoryImpl", "🚨 스택 트레이스: ", e)
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }
    
    

    override suspend fun getVolumeData(
        stockCode: String,
        timeFrame: String,
        period: Int
    ): Flow<Resource<List<VolumeData>>> = flow {
        try {
            emit(Resource.Loading())
            
            // 1. 메모리 캐시에서 먼저 확인
            val cachedData = memoryCache.getVolumeData(stockCode, timeFrame)
            if (cachedData != null) {
                emit(Resource.Success(cachedData))
                return@flow
            }
            
            // 2. 캐시에 없으면 API 호출
            val response = apiService.getVolumeData(stockCode, timeFrame, period)
            if (response.success) {
                val volumeData = response.data.toVolumeDataList()
                
                // 3. 메모리 캐시에 저장 (15분 TTL)
                memoryCache.putVolumeData(stockCode, timeFrame, volumeData, 15)
                
                emit(Resource.Success(volumeData))
            } else {
                emit(Resource.Error("Failed to fetch volume data"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getIndicators(
        stockCode: String,
        indicators: List<String>,
        timeFrame: String,
        period: Int
    ): Flow<Resource<ChartIndicatorData>> = flow {
        try {
            emit(Resource.Loading())
            
            // 1. 메모리 캐시에서 먼저 확인
            val cachedData = memoryCache.getIndicatorData(stockCode, timeFrame, indicators)
            if (cachedData != null) {
                emit(Resource.Success(cachedData))
                return@flow
            }
            
            // 2. 캐시에 없으면 API 호출
            val indicatorsQuery = indicators.joinToString(",")
            val response = apiService.getIndicators(stockCode, indicatorsQuery, timeFrame, period)
            
            if (response.success) {
                val data = response.data
                val indicatorData = ChartIndicatorData(
                    sma5 = data.sma5?.toLineDataList() ?: emptyList(),
                    sma20 = data.sma20?.toLineDataList() ?: emptyList(),
                    sma60 = data.sma60?.toLineDataList() ?: emptyList(),
                    sma120 = data.sma120?.toLineDataList() ?: emptyList(),
                    rsi = data.rsi?.toLineDataList() ?: emptyList(),
                    macd = data.macd?.toDomain(),
                    bollingerBands = data.bollingerBands?.toDomain()
                )
                
                // 3. 메모리 캐시에 저장 (30분 TTL)
                memoryCache.putIndicatorData(stockCode, timeFrame, indicators, indicatorData, 30)
                
                emit(Resource.Success(indicatorData))
            } else {
                emit(Resource.Error("Failed to fetch indicators"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getUserHoldings(): Flow<Resource<List<HoldingItem>>> = flow {
        try {
            emit(Resource.Loading())
            val token = userPreferences.getAccessToken()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Authentication required"))
                return@flow
            }
            
            val response = apiService.getUserHoldings("Bearer $token")
            if (response.success) {
                emit(Resource.Success(response.data.toHoldingItemList()))
            } else {
                emit(Resource.Error("Failed to fetch holdings"))
            }
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> emit(Resource.Error("Authentication failed"))
                403 -> emit(Resource.Error("Access denied"))
                else -> emit(Resource.Error("Network error: ${e.localizedMessage}"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getTradingHistory(
        stockCode: String?,
        page: Int,
        size: Int
    ): Flow<Resource<TradingHistoryPage>> = flow {
        try {
            emit(Resource.Loading())
            val token = userPreferences.getAccessToken()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Authentication required"))
                return@flow
            }
            
            val response = apiService.getTradingHistory("Bearer $token", stockCode, page, size)
            if (response.success) {
                val pageData = response.data
                val tradingHistoryPage = TradingHistoryPage(
                    content = pageData.content.toTradingItemList(),
                    page = pageData.page,
                    size = pageData.size,
                    totalElements = pageData.totalElements,
                    totalPages = pageData.totalPages
                )
                emit(Resource.Success(tradingHistoryPage))
            } else {
                emit(Resource.Error("Failed to fetch trading history"))
            }
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> emit(Resource.Error("Authentication failed"))
                403 -> emit(Resource.Error("Access denied"))
                else -> emit(Resource.Error("Network error: ${e.localizedMessage}"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun addToFavorites(stockCode: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            val token = userPreferences.getAccessToken()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Authentication required"))
                return@flow
            }
            
            val response = apiService.addToFavorites("Bearer $token", FavoriteStockRequest(stockCode))
            emit(Resource.Success(response.success))
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> emit(Resource.Error("Authentication failed"))
                403 -> emit(Resource.Error("Access denied"))
                else -> emit(Resource.Error("Network error: ${e.localizedMessage}"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun removeFromFavorites(stockCode: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            val token = userPreferences.getAccessToken()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Authentication required"))
                return@flow
            }
            
            val response = apiService.removeFromFavorites("Bearer $token", stockCode)
            emit(Resource.Success(response.success))
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> emit(Resource.Error("Authentication failed"))
                403 -> emit(Resource.Error("Access denied"))
                else -> emit(Resource.Error("Network error: ${e.localizedMessage}"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getFavorites(): Flow<Resource<List<String>>> = flow {
        try {
            emit(Resource.Loading())
            val token = userPreferences.getAccessToken()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Authentication required"))
                return@flow
            }
            
            val response = apiService.getFavorites("Bearer $token")
            if (response.success) {
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Error("Failed to fetch favorites"))
            }
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> emit(Resource.Error("Authentication failed"))
                403 -> emit(Resource.Error("Access denied"))
                else -> emit(Resource.Error("Network error: ${e.localizedMessage}"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun isFavorite(stockCode: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            getFavorites().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        emit(Resource.Success(resource.data?.contains(stockCode) == true))
                    }
                    is Resource.Error -> {
                        emit(Resource.Error(resource.message ?: "Failed to check favorite status"))
                    }
                    is Resource.Loading -> {
                        emit(Resource.Loading())
                    }
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun analyzeChartPattern(
        request: com.lago.app.data.remote.dto.PatternAnalysisRequest
    ): Flow<Resource<List<com.lago.app.data.remote.dto.PatternAnalysisResponse>>> = flow {
        try {
            emit(Resource.Loading())
            android.util.Log.d("ChartRepositoryImpl", "🔍 차트 패턴 분석 시작: ${request.stockCode}")
            
            val response = apiService.analyzeChartPattern(request)
            android.util.Log.d("ChartRepositoryImpl", "🔍 패턴 분석 성공: ${response.size}개 패턴 발견")
            
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            android.util.Log.e("ChartRepositoryImpl", "🚨 패턴 분석 HTTP 오류: ${e.code()} - ${e.message()}")
            when (e.code()) {
                401 -> emit(Resource.Error("Authentication failed"))
                403 -> emit(Resource.Error("Access denied"))
                400 -> emit(Resource.Error("Invalid request parameters"))
                else -> emit(Resource.Error("Network error: ${e.localizedMessage}"))
            }
        } catch (e: IOException) {
            android.util.Log.e("ChartRepositoryImpl", "🚨 패턴 분석 네트워크 오류", e)
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            android.util.Log.e("ChartRepositoryImpl", "🚨 패턴 분석 예상치 못한 오류", e)
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }
    
    override suspend fun getHistoricalCandlestickData(
        stockCode: String,
        timeFrame: String,
        beforeTime: Long?,
        limit: Int
    ): Flow<Resource<List<CandlestickData>>> = flow {
        try {
            emit(Resource.Loading())
            
            // 주식 코드를 주식 ID로 변환
            val stockId = StockCodeMapper.getStockId(stockCode)
            if (stockId == null) {
                emit(Resource.Error("Unknown stock code: $stockCode"))
                return@flow
            }
            
            // beforeTime 기준으로 과거 데이터 계산
            val endTime = beforeTime?.let { Date(it) } ?: Date()
            
            val candlestickData = when (timeFrame) {
                "D" -> {
                    // 일별 데이터
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val calendar = Calendar.getInstance().apply { time = endTime }
                    calendar.add(Calendar.DAY_OF_MONTH, -1) // beforeTime 하루 전부터
                    val endDate = dateFormat.format(calendar.time)
                    calendar.add(Calendar.DAY_OF_MONTH, -limit) 
                    val startDate = dateFormat.format(calendar.time)
                    
                    val response = apiService.getStockDayData(stockId, startDate, endDate)
                    response.reversed() // 최신순 -> 과거순 정렬
                        .map { dto ->
                            CandlestickData(
                                time = ChartTimeManager.parseDate(dto.date),
                                open = dto.openPrice.toFloat(),
                                high = dto.highPrice.toFloat(),
                                low = dto.lowPrice.toFloat(),
                                close = dto.closePrice.toFloat(),
                                volume = dto.volume.toLong()
                            )
                        }
                }
                "1", "3", "5", "15", "30", "60" -> {
                    // 분봉 데이터
                    val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val calendar = Calendar.getInstance().apply { time = endTime }
                    calendar.add(Calendar.HOUR_OF_DAY, -1) // beforeTime 1시간 전부터
                    val endDateTime = dateTimeFormat.format(calendar.time)
                    calendar.add(Calendar.HOUR_OF_DAY, -limit)
                    val startDateTime = dateTimeFormat.format(calendar.time)
                    
                    val response = apiService.getStockMinuteData(stockId, startDateTime, endDateTime)
                    response.reversed() // 최신순 -> 과거순 정렬
                        .map { dto ->
                            CandlestickData(
                                time = ChartTimeManager.parseDateTime(dto.date),
                                open = dto.openPrice.toFloat(),
                                high = dto.highPrice.toFloat(),
                                low = dto.lowPrice.toFloat(),
                                close = dto.closePrice.toFloat(),
                                volume = dto.volume.toLong()
                            )
                        }
                }
                "M" -> {
                    // 월별 데이터
                    val calendar = Calendar.getInstance().apply { time = endTime }
                    val endMonth = calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)
                    calendar.add(Calendar.MONTH, -limit)
                    val startMonth = calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)
                    
                    val response = apiService.getStockMonthData(stockId, startMonth, endMonth)
                    response.reversed() // 최신순 -> 과거순 정렬
                        .map { dto ->
                            CandlestickData(
                                time = ChartTimeManager.parseMonthDate(dto.date),
                                open = dto.openPrice.toFloat(),
                                high = dto.highPrice.toFloat(),
                                low = dto.lowPrice.toFloat(),
                                close = dto.closePrice.toFloat(),
                                volume = dto.volume.toLong()
                            )
                        }
                }
                "Y" -> {
                    // 년별 데이터
                    val calendar = Calendar.getInstance().apply { time = endTime }
                    val endYear = calendar.get(Calendar.YEAR)
                    val startYear = endYear - limit
                    
                    val response = apiService.getStockYearData(stockId, startYear, endYear)
                    response.reversed() // 최신순 -> 과거순 정렬
                        .map { dto ->
                            CandlestickData(
                                time = ChartTimeManager.parseYearDate(dto.date),
                                open = dto.openPrice.toFloat(),
                                high = dto.highPrice.toFloat(),
                                low = dto.lowPrice.toFloat(),
                                close = dto.closePrice.toFloat(),
                                volume = dto.volume.toLong()
                            )
                        }
                }
                else -> {
                    emit(Resource.Error("Unsupported timeframe: $timeFrame"))
                    return@flow
                }
            }
            
            emit(Resource.Success(candlestickData))
            
        } catch (e: HttpException) {
            emit(Resource.Error("HTTP error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("과거 데이터 로딩 실패: ${e.message}"))
        }
    }
    

    // ===== 역사챌린지 관련 메서드 구현 =====

    override suspend fun getHistoryChallenge(): Flow<Resource<com.lago.app.data.remote.dto.HistoryChallengeResponse>> = flow {
        try {
            emit(Resource.Loading())
            android.util.Log.d("ChartRepositoryImpl", "🔥 getHistoryChallenge 호출 시작 (인증 없이)")

            android.util.Log.d("ChartRepositoryImpl", "🔥 API 호출: /api/history-challenge (토큰 없이)")
            val response = apiService.getHistoryChallenge()
            android.util.Log.d("ChartRepositoryImpl", "🔥 API 응답 성공: ${response.stockName}")
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            android.util.Log.e("ChartRepositoryImpl", "🚨 HTTP 오류: ${e.code()} - ${e.message()}")
            when (e.code()) {
                401 -> emit(Resource.Error("Authentication failed"))
                403 -> emit(Resource.Error("Access denied"))
                else -> emit(Resource.Error("Network error: ${e.localizedMessage}"))
            }
        } catch (e: IOException) {
            android.util.Log.e("ChartRepositoryImpl", "🚨 네트워크 연결 실패", e)
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            android.util.Log.e("ChartRepositoryImpl", "🚨 예상치 못한 오류", e)
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getHistoryChallengeChart(
        challengeId: Int,
        interval: String,
        pastMinutes: Int?,
        pastDays: Int?
    ): Flow<Resource<List<CandlestickData>>> = flow {
        try {
            emit(Resource.Loading())

            // 현재 시간 기준으로 과거 기간 계산
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val calendar = java.util.Calendar.getInstance()
            val toDateTime = dateFormat.format(calendar.time) // 현재 시간
            
            // 과거 시간 계산
            when {
                pastMinutes != null -> calendar.add(java.util.Calendar.MINUTE, -pastMinutes)
                pastDays != null -> calendar.add(java.util.Calendar.DAY_OF_MONTH, -pastDays)
                else -> calendar.add(java.util.Calendar.DAY_OF_MONTH, -100) // 기본 100일
            }
            val fromDateTime = dateFormat.format(calendar.time)

            android.util.Log.d("ChartRepositoryImpl", "🔥 역사챌린지 차트 API 호출: $fromDateTime ~ $toDateTime")

            // 내부 시간프레임을 API 시간프레임으로 변환
            val apiInterval = ChartTimeManager.toApiTimeFrame(interval)
            android.util.Log.d("ChartRepositoryImpl", "🔥 시간프레임 변환: $interval → $apiInterval")

            // 역사챌린지는 인증 없이 호출
            val response = apiService.getHistoryChallengeChart(
                challengeId = challengeId,
                interval = apiInterval,
                fromDateTime = fromDateTime,
                toDateTime = toDateTime
            )

            // Convert HistoryChallengeDataResponse to CandlestickData
            val candlestickData = response.map { dto ->
                CandlestickData(
                    time = ChartTimeManager.parseHistoryChallengeDateTime(dto.originDateTime),
                    open = dto.openPrice.toFloat(),
                    high = dto.highPrice.toFloat(),
                    low = dto.lowPrice.toFloat(),
                    close = dto.closePrice.toFloat(),
                    volume = dto.volume.toLong()
                )
            }

            emit(Resource.Success(candlestickData))
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> emit(Resource.Error("Authentication failed"))
                403 -> emit(Resource.Error("Access denied"))
                404 -> emit(Resource.Error("Challenge not found"))
                else -> emit(Resource.Error("Network error: ${e.localizedMessage}"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getHistoryChallengeHistoricalData(
        challengeId: Int,
        interval: String,
        beforeDateTime: String,
        limit: Int
    ): Flow<Resource<List<CandlestickData>>> = flow {
        try {
            emit(Resource.Loading())

            // 내부 시간프레임을 API 시간프레임으로 변환
            val apiInterval = ChartTimeManager.toApiTimeFrame(interval)
            
            android.util.Log.d("ChartRepositoryImpl", "🔥 역사챌린지 무한 히스토리 API 호출")
            android.util.Log.d("ChartRepositoryImpl", "🔥 파라미터: challengeId=$challengeId, interval=$interval → $apiInterval")
            android.util.Log.d("ChartRepositoryImpl", "🔥 beforeDateTime=$beforeDateTime, limit=$limit")

            // 역사챌린지 무한 히스토리 API 호출 (인증 없음)
            val response = apiService.getHistoryChallengeHistoricalData(
                challengeId = challengeId,
                interval = apiInterval,
                beforeDateTime = beforeDateTime,
                limit = limit
            )

            // Convert HistoryChallengeDataResponse to CandlestickData
            val candlestickData = response.map { dto ->
                CandlestickData(
                    time = ChartTimeManager.parseHistoryChallengeDateTime(dto.originDateTime),
                    open = dto.openPrice.toFloat(),
                    high = dto.highPrice.toFloat(),
                    low = dto.lowPrice.toFloat(),
                    close = dto.closePrice.toFloat(),
                    volume = dto.volume.toLong()
                )
            }.sortedBy { it.time } // 시간순 정렬

            android.util.Log.d("ChartRepositoryImpl", "🔥 무한 히스토리 데이터 로드 성공: ${candlestickData.size}개 캔들")
            
            emit(Resource.Success(candlestickData))
        } catch (e: HttpException) {
            android.util.Log.e("ChartRepositoryImpl", "🚨 무한 히스토리 HTTP 오류: ${e.code()} - ${e.message()}")
            when (e.code()) {
                401 -> emit(Resource.Error("Authentication failed"))
                403 -> emit(Resource.Error("Access denied"))
                404 -> emit(Resource.Error("Historical data not found"))
                else -> emit(Resource.Error("Network error: ${e.localizedMessage}"))
            }
        } catch (e: IOException) {
            android.util.Log.e("ChartRepositoryImpl", "🚨 무한 히스토리 네트워크 오류", e)
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            android.util.Log.e("ChartRepositoryImpl", "🚨 무한 히스토리 예상치 못한 오류", e)
            emit(Resource.Error("Historical data loading failed: ${e.localizedMessage}"))
        }
    }

    override suspend fun getHistoryChallengeNews(
        challengeId: Int,
        pastDateTime: String
    ): Flow<Resource<List<com.lago.app.data.remote.dto.HistoryChallengeNewsResponse>>> = flow {
        try {
            emit(Resource.Loading())

            val token = userPreferences.getAccessToken()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Authentication required"))
                return@flow
            }

            val response = apiService.getHistoryChallengeNews(
                authorization = "Bearer $token",
                challengeId = challengeId,
                pastDateTime = pastDateTime
            )

            emit(Resource.Success(response))
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> emit(Resource.Error("Authentication failed"))
                403 -> emit(Resource.Error("Access denied"))
                404 -> emit(Resource.Error("Challenge not found"))
                else -> emit(Resource.Error("Network error: ${e.localizedMessage}"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }



}
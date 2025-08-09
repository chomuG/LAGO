package com.lago.app.data.repository

import com.lago.app.data.local.prefs.UserPreferences
import com.lago.app.data.mapper.ChartDataMapper
import com.lago.app.data.mapper.ChartDataMapper.toCandlestickDataList
import com.lago.app.data.mapper.ChartDataMapper.toDomain
import com.lago.app.data.mapper.ChartDataMapper.toHoldingItemList
import com.lago.app.data.mapper.ChartDataMapper.toLineDataList
import com.lago.app.data.mapper.ChartDataMapper.toTradingItemList
import com.lago.app.data.mapper.ChartDataMapper.toVolumeDataList
import com.lago.app.data.remote.api.ChartApiService
import com.lago.app.domain.entity.*
import com.lago.app.domain.repository.ChartRepository
import com.lago.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChartRepositoryImpl @Inject constructor(
    private val apiService: ChartApiService,
    private val userPreferences: UserPreferences
) : ChartRepository {

    override suspend fun getStockInfo(stockCode: String): Flow<Resource<StockInfo>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getStockInfo(stockCode)
            if (response.success) {
                emit(Resource.Success(response.data.toDomain()))
            } else {
                emit(Resource.Error("Failed to fetch stock info"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getCandlestickData(
        stockCode: String,
        timeFrame: String,
        period: Int
    ): Flow<Resource<List<CandlestickData>>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getCandlestickData(stockCode, timeFrame, period)
            if (response.success) {
                emit(Resource.Success(response.data.toCandlestickDataList()))
            } else {
                emit(Resource.Error("Failed to fetch candlestick data"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
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
            val response = apiService.getVolumeData(stockCode, timeFrame, period)
            if (response.success) {
                emit(Resource.Success(response.data.toVolumeDataList()))
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
            val token = userPreferences.getAuthToken()
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
            val token = userPreferences.getAuthToken()
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
            val token = userPreferences.getAuthToken()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Authentication required"))
                return@flow
            }
            
            val response = apiService.addToFavorites("Bearer $token", stockCode)
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
            val token = userPreferences.getAuthToken()
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
            val token = userPreferences.getAuthToken()
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
        stockCode: String,
        timeFrame: String,
        startTime: String?,
        endTime: String?
    ): Flow<Resource<PatternAnalysisResult>> = flow {
        try {
            emit(Resource.Loading())
            
            val token = userPreferences.getAuthToken()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Authentication required"))
                return@flow
            }
            
            val request = com.lago.app.data.remote.dto.PatternAnalysisRequest(
                timeFrame = timeFrame,
                startTime = startTime,
                endTime = endTime
            )
            
            val response = apiService.analyzeChartPattern("Bearer $token", stockCode, request)
            
            if (response.success) {
                val patternResult = PatternAnalysisResult(
                    patterns = response.data.patterns.map { pattern ->
                        PatternItem(
                            patternName = pattern.patternName,
                            description = pattern.description,
                            confidence = pattern.confidence,
                            recommendation = pattern.recommendation
                        )
                    },
                    analysisTime = response.data.analysisTime,
                    confidenceScore = response.data.confidenceScore
                )
                emit(Resource.Success(patternResult))
            } else {
                emit(Resource.Error("Pattern analysis failed"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Connection error: ${e.localizedMessage}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }
}
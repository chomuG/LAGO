package com.lago.app.data.repository

import com.lago.app.data.local.prefs.UserPreferences
import com.lago.app.data.mapper.ChartDataMapper.toDomain
import com.lago.app.data.mapper.ChartDataMapper.toStockItemList
import com.lago.app.data.mapper.ChartDataMapper.toStockItemListFromSimple
import com.lago.app.data.mapper.ChartDataMapper.toStockListPage
import com.lago.app.data.remote.api.ChartApiService
import com.lago.app.data.remote.dto.FavoriteStockRequest
import com.lago.app.domain.entity.StockItem
import com.lago.app.domain.entity.StockListPage
import com.lago.app.domain.repository.StockListRepository
import com.lago.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockListRepositoryImpl @Inject constructor(
    private val apiService: ChartApiService,
    private val userPreferences: UserPreferences
) : StockListRepository {

    override suspend fun getStockList(
        category: String?,
        page: Int,
        size: Int,
        sort: String,
        search: String?
    ): Flow<Resource<StockListPage>> = flow {
        try {
            emit(Resource.Loading())
            android.util.Log.d("StockListRepo", "🔥 API 요청 시작: category=$category, page=$page, size=$size, search=$search")
            
            // 기존 API 사용 (백엔드에 /api/stocks/list 엔드포인트가 없음)
            val stockList = if (search.isNullOrEmpty()) {
                android.util.Log.d("StockListRepo", "🔥 getStockList 호출: category=$category")
                apiService.getStockList(
                    category = category,
                    page = page,
                    size = size,
                    sort = sort,
                    search = null
                )
            } else {
                android.util.Log.d("StockListRepo", "🔥 searchStocks 호출: query=$search")
                apiService.searchStocks(
                    query = search,
                    page = page,
                    size = size
                )
            }
            
            android.util.Log.d("StockListRepo", "🔥 API 응답 수신: ${stockList.size}개 종목")
            
            if (stockList.isNotEmpty()) {
                android.util.Log.d("StockListRepo", "🔥 API 성공 응답: ${stockList.size}개 종목 수신")
                stockList.forEachIndexed { index, stock ->
                    android.util.Log.d("StockListRepo", "📋 [$index] ${stock.code} (${stock.name}) - ${stock.market}")
                }
                
                // SimpleStockDto를 StockItem으로 변환 (백엔드 DTO 구조에 맞춰 수정)
                val stockItems = stockList.map { dto ->
                    StockItem(
                        code = dto.code,
                        name = dto.name,
                        market = dto.market, // 백엔드에서 제공되는 market 정보 사용
                        currentPrice = 0, // 기본값, WebSocket으로 실시간 업데이트
                        priceChange = 0, // 기본값, WebSocket으로 실시간 업데이트
                        priceChangePercent = 0.0, // 기본값, WebSocket으로 실시간 업데이트
                        volume = 0L, // 기본값, WebSocket으로 실시간 업데이트
                        marketCap = null, // 백엔드 StockInfoDto에서 제공하지 않음
                        sector = null, // 백엔드 StockInfoDto에서 제공하지 않음
                        isFavorite = false, // 관심종목 여부는 별도 API로 조회
                        updatedAt = java.time.LocalDateTime.now().toString()
                    )
                }
                
                android.util.Log.d("StockListRepo", "🔥 ${stockItems.size}개 종목 변환 완료 (WebSocket에서 실시간 데이터 업데이트 예정)")
                
                // 페이징 정보 (기존 API는 페이징 미지원, 전체 목록 반환)
                val stockListPage = StockListPage(
                    content = stockItems,
                    page = page,
                    size = stockItems.size,
                    totalElements = stockItems.size.toLong(),
                    totalPages = 1
                )
                
                emit(Resource.Success(stockListPage))
            } else {
                android.util.Log.w("StockListRepo", "🚨 빈 주식 목록 수신")
                emit(Resource.Error("주식 목록이 비어있습니다"))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("StockListRepo", "🚨 HTTP 에러: ${e.code()} - ${e.message()}")
            android.util.Log.e("StockListRepo", "🚨 에러 상세: $errorBody")
            emit(Resource.Error("HTTP ${e.code()}: ${e.message()}"))
        } catch (e: IOException) {
            android.util.Log.e("StockListRepo", "🚨 네트워크 연결 실패", e)
            emit(Resource.Error("Network connection failed: ${e.localizedMessage}"))
        } catch (e: Exception) {
            android.util.Log.e("StockListRepo", "🚨 예상치 못한 에러", e)
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getTrendingStocks(limit: Int): Flow<Resource<List<StockItem>>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getTrendingStocks(limit)
            emit(Resource.Success(response.toStockItemListFromSimple()))
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun searchStocks(
        query: String,
        page: Int,
        size: Int
    ): Flow<Resource<StockListPage>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.searchStocks(query, page, size)
            emit(Resource.Success(response.toStockListPage()))
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    override suspend fun toggleFavorite(stockCode: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            val token = userPreferences.getAccessToken()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Authentication required"))
                return@flow
            }

            // 현재 관심종목 상태 확인
            val isFavoriteResult = isFavorite(stockCode)
            var currentIsFavorite = false
            
            isFavoriteResult.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        currentIsFavorite = resource.data == true
                    }
                    is Resource.Error -> {
                        emit(Resource.Error("Failed to check favorite status"))
                        return@collect
                    }
                    is Resource.Loading -> {
                        // Continue
                    }
                }
            }

            // 상태에 따라 추가 또는 제거
            val response = if (currentIsFavorite) {
                apiService.removeFromFavorites("Bearer $token", stockCode)
            } else {
                apiService.addToFavorites("Bearer $token", FavoriteStockRequest(stockCode))
            }

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

    override suspend fun isFavorite(stockCode: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            val token = userPreferences.getAccessToken()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Authentication required"))
                return@flow
            }

            val response = apiService.getFavorites("Bearer $token")
            if (response.success) {
                emit(Resource.Success(response.data.contains(stockCode)))
            } else {
                emit(Resource.Error("Failed to check favorite status"))
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

    override suspend fun getFavoriteStocks(
        page: Int,
        size: Int
    ): Flow<Resource<StockListPage>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getStockList("favorites", page, size)
            emit(Resource.Success(response.toStockListPage()))
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
}
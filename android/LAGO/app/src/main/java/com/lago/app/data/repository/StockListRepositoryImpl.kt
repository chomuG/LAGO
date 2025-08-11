package com.lago.app.data.repository

import com.lago.app.data.local.prefs.UserPreferences
import com.lago.app.data.mapper.ChartDataMapper.toDomain
import com.lago.app.data.mapper.ChartDataMapper.toStockItemList
import com.lago.app.data.mapper.ChartDataMapper.toStockItemListFromSimple
import com.lago.app.data.mapper.ChartDataMapper.toStockListPage
import com.lago.app.data.remote.api.ChartApiService
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
            val response = apiService.getStockList(category, page, size, sort, search)
            emit(Resource.Success(response.toStockListPage()))
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed"))
        } catch (e: Exception) {
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
            val token = userPreferences.getAuthToken()
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
                apiService.addToFavorites("Bearer $token", stockCode)
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
            val token = userPreferences.getAuthToken()
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
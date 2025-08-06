package com.lago.app.domain.repository

import com.lago.app.domain.entity.StockItem
import com.lago.app.domain.entity.StockListPage
import com.lago.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockListRepository {
    
    /**
     * 주식 목록 조회 (카테고리별)
     */
    suspend fun getStockList(
        category: String? = null,
        page: Int = 0,
        size: Int = 20,
        sort: String = "code",
        search: String? = null
    ): Flow<Resource<StockListPage>>
    
    /**
     * 인기 주식 목록 조회
     */
    suspend fun getTrendingStocks(
        limit: Int = 20
    ): Flow<Resource<List<StockItem>>>
    
    /**
     * 주식 검색
     */
    suspend fun searchStocks(
        query: String,
        page: Int = 0,
        size: Int = 20
    ): Flow<Resource<StockListPage>>
    
    /**
     * 관심종목 토글 (추가/제거)
     */
    suspend fun toggleFavorite(stockCode: String): Flow<Resource<Boolean>>
    
    /**
     * 관심종목 상태 확인
     */
    suspend fun isFavorite(stockCode: String): Flow<Resource<Boolean>>
    
    /**
     * 관심종목 목록 조회 (StockItem 형태로)
     */
    suspend fun getFavoriteStocks(
        page: Int = 0,
        size: Int = 20
    ): Flow<Resource<StockListPage>>
}
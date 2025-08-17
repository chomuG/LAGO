package com.lago.app.presentation.viewmodel.stocklist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.StockItem
import com.lago.app.domain.entity.HistoryChallengeStock
import com.lago.app.domain.entity.CandlestickData
import com.lago.app.domain.repository.StockListRepository
import com.lago.app.domain.repository.HistoryChallengeRepository
import com.lago.app.domain.repository.ChartRepository
import com.lago.app.domain.repository.MockTradeRepository
import com.lago.app.data.cache.FavoriteCache
import com.lago.app.data.remote.websocket.SmartStockWebSocketService
import com.lago.app.data.scheduler.SmartUpdateScheduler
import com.lago.app.domain.entity.ScreenType
import com.lago.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.Duration.Companion.milliseconds
import javax.inject.Inject

enum class SortType {
    NONE,
    NAME_ASC,
    NAME_DESC,
    PRICE_ASC,
    PRICE_DESC,
    CHANGE_ASC,
    CHANGE_DESC
}

data class StockListUiState(
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val selectedFilters: List<String> = emptyList(),
    val stocks: List<StockItem> = emptyList(),
    val filteredStocks: List<StockItem> = emptyList(),
    val historyChallengeStocks: List<HistoryChallengeStock> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentPage: Int = 0,
    val hasMoreData: Boolean = true,
    val currentSortType: SortType = SortType.NAME_ASC,  // 초기값: 이름 오름차순
    val showFavoritesOnly: Boolean = false  // 관심목록 필터
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class StockListViewModel @Inject constructor(
    private val stockListRepository: StockListRepository,
    private val historyChallengeRepository: HistoryChallengeRepository,
    private val chartRepository: com.lago.app.domain.repository.ChartRepository,
    private val smartWebSocketService: SmartStockWebSocketService,
    private val smartUpdateScheduler: SmartUpdateScheduler,
    private val realTimeCache: com.lago.app.data.cache.RealTimeStockCache,
    private val mockTradeRepository: MockTradeRepository,
    private val favoriteCache: FavoriteCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockListUiState())
    val uiState: StateFlow<StockListUiState> = _uiState

    init {
        // 1. WebSocket 연결
        smartWebSocketService.connect()
        
        // 2. 캐시 StateFlow 구독 (자동 UI 업데이트)
        observeRealTimeData()
        observeFavoriteData()
        
        // 3. 초기 데이터 로드
        loadStocks()
        loadHistoryChallengeStocks()
        loadUserFavorites()
        
        // 4. 역사챌린지 실시간 데이터 업데이트 관제 (예정)
        // observeHistoryChallengeRealTimeData()
    }
    
    private fun observeRealTimeData() {
        // 캐시의 StateFlow를 직접 구독 (자동 UI 업데이트)
        viewModelScope.launch {
            realTimeCache.quotes
                .sample(250.milliseconds) // 250ms마다 샘플링 (렌더링 빈도 제한)
                .collect { quotesMap ->
                    android.util.Log.d("StockListViewModel", "📊 캐시 업데이트 수신: ${quotesMap.size}개 종목")
                    if (quotesMap.isNotEmpty()) {
                        quotesMap.forEach { (code, data) ->
                            android.util.Log.v("StockListViewModel", "  - $code: ${data.price.toInt()}원")
                        }
                        android.util.Log.w("StockListViewModel", "🔥 캐시에서 ${quotesMap.size}개 종목 업데이트")
                        updateStocksWithCachedData(quotesMap)
                    } else {
                        android.util.Log.d("StockListViewModel", "📊 캐시가 비어있음")
                    }
                }
        }
    }
    
    private fun updateStocksWithCachedData(quotesMap: Map<String, com.lago.app.domain.entity.StockRealTimeData>) {
        android.util.Log.d("StockListViewModel", "🔄 UI 업데이트 시작: ${quotesMap.size}개 실시간 데이터 처리")
        
        _uiState.update { currentState ->
            android.util.Log.d("StockListViewModel", "🔄 현재 ${currentState.stocks.size}개 종목이 UI에 있음")
            
            var updateCount = 0
            val updatedStocks = currentState.stocks.map { stock ->
                val realTimeData = quotesMap[stock.code]
                if (realTimeData != null) {
                    android.util.Log.v("StockListViewModel", "🔍 ${stock.code}: 실시간 데이터 있음 (${realTimeData.price.toInt()}원)")
                    
                    if (stock.currentPrice != realTimeData.price.toInt() ||
                        stock.volume != (realTimeData.volume ?: 0L)) {
                        
                        android.util.Log.d("StockListViewModel", "💰 ${stock.code}: ${stock.currentPrice}→${realTimeData.price.toInt()}원")
                        updateCount++
                        
                        stock.copy(
                            currentPrice = realTimeData.price.toInt(),
                            priceChange = realTimeData.priceChange.toInt(),
                            priceChangePercent = realTimeData.priceChangePercent,
                            volume = realTimeData.volume ?: 0L
                        )
                    } else {
                        android.util.Log.v("StockListViewModel", "🔍 ${stock.code}: 가격 변화 없음")
                        stock
                    }
                } else {
                    // 실시간 데이터가 없으면 캐시에서 마지막 알려진 데이터 확인
                    val cachedData = realTimeCache.getStockData(stock.code)
                    if (cachedData != null) {
                        android.util.Log.v("StockListViewModel", "🔍 ${stock.code}: 캐시에서 마지막 데이터 적용 (${cachedData.price.toInt()}원)")
                        stock.copy(
                            currentPrice = cachedData.price.toInt(),
                            priceChange = cachedData.priceChange.toInt(),
                            priceChangePercent = cachedData.priceChangePercent,
                            volume = cachedData.volume ?: 0L
                        )
                    } else {
                        android.util.Log.v("StockListViewModel", "🔍 ${stock.code}: 캐시에도 데이터 없음")
                        stock
                    }
                }
            }
            
            // 역사챌린지는 전용 WebSocket 채널 (/topic/history-challenge) 사용
            // 캐시에서 역사챌린지 종목 실시간 데이터 가져와서 업데이트
            android.util.Log.d("StockListViewModel", "🔥 역사챌린지 업데이트 시도: ${currentState.historyChallengeStocks.size}개 종목")
            currentState.historyChallengeStocks.forEach { stock ->
                val historyChallengeKey = "HISTORY_CHALLENGE_${stock.stockCode}"
                val hasData = quotesMap.containsKey(historyChallengeKey)
                android.util.Log.d("StockListViewModel", "🔥 역사챌린지 ${stock.stockCode} (키: $historyChallengeKey): 캐시 데이터 $hasData")
                if (hasData) {
                    val data = quotesMap[historyChallengeKey]!!
                    android.util.Log.d("StockListViewModel", "🔥 역사챌린지 ${stock.stockCode} 캐시 데이터: price=${data.price}, changePrice=${data.priceChange}, fluctuationRate=${data.priceChangePercent}")
                }
            }
            val updatedHistoryStocks = updateHistoryChallengeStocksWithCache(currentState.historyChallengeStocks, quotesMap)
            
            android.util.Log.d("StockListViewModel", "✅ UI 업데이트 완료: 일반 ${updateCount}개 종목 변경됨 (역사챌린지는 별도 채널)")
            
            currentState.copy(
                stocks = updatedStocks,
                filteredStocks = applyFiltersAndSort(updatedStocks),
                historyChallengeStocks = updatedHistoryStocks
            )
        }
    }
    
    // 가시 영역 종목 업데이트 (LazyColumn 스크롤 시 호출)
    fun updateVisibleStocks(visibleStockCodes: List<String>) {
        smartWebSocketService.updateVisibleStocks(visibleStockCodes)
    }
    
    private fun applyFiltersAndSort(stocks: List<StockItem>): List<StockItem> {
        val currentState = _uiState.value
        var filtered = stocks

        // 검색어 필터
        if (currentState.searchQuery.isNotEmpty()) {
            val query = currentState.searchQuery.lowercase()
            filtered = filtered.filter { stock ->
                stock.name.lowercase().contains(query) || 
                stock.code.lowercase().contains(query)
            }
        }

        // 관심목록 필터
        if (currentState.showFavoritesOnly) {
            filtered = filtered.filter { it.isFavorite }
        }

        // 정렬 적용
        filtered = when (currentState.currentSortType) {
            SortType.NAME_ASC -> filtered.sortedBy { it.name }
            SortType.NAME_DESC -> filtered.sortedByDescending { it.name }
            SortType.PRICE_ASC -> filtered.sortedBy { it.currentPrice }
            SortType.PRICE_DESC -> filtered.sortedByDescending { it.currentPrice }
            SortType.CHANGE_ASC -> filtered.sortedBy { it.priceChangePercent }
            SortType.CHANGE_DESC -> filtered.sortedByDescending { it.priceChangePercent }
            SortType.NONE -> filtered
        }

        return filtered
    }

    private fun loadStocks(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(currentPage = 0, hasMoreData = true, stocks = emptyList()) }
            }
            
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val currentState = _uiState.value
            val category = when (currentState.selectedTab) {
                0 -> null // 전체
                1 -> "kospi"
                2 -> "kosdaq"
                3 -> "favorites"
                else -> null
            }

            stockListRepository.getStockList(
                category = category,
                page = currentState.currentPage,
                size = 20,
                sort = getSortType(),
                search = if (currentState.searchQuery.isNotEmpty()) currentState.searchQuery else null
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val stockListPage = resource.data!!
                        val newStocks = if (isRefresh || currentState.currentPage == 0) {
                            stockListPage.content
                        } else {
                            currentState.stocks + stockListPage.content
                        }
                        
                        android.util.Log.w("StockListViewModel", "🔥 API에서 종목 로딩 성공: ${newStocks.size}개")
                        
                        // 🚀 점진적 로딩: 1단계 - 종목 정보 먼저 표시 (가격은 0원이어도 OK)
                        val stocksWithCachedData = mergeWithCachedData(newStocks)
                        android.util.Log.w("StockListViewModel", "🔥 캐시 병합 완료: ${stocksWithCachedData.count { it.currentPrice > 0 }}개 실시간 가격 적용")
                        
                        // 즉시 UI 업데이트 - 종목 정보 먼저 표시
                        _uiState.update {
                            it.copy(
                                stocks = stocksWithCachedData,
                                filteredStocks = stocksWithCachedData,
                                isLoading = false,
                                errorMessage = null,
                                currentPage = stockListPage.page,
                                hasMoreData = stockListPage.page < stockListPage.totalPages - 1
                            )
                        }
                        filterStocks()
                        
                        // 🚀 점진적 로딩: 2단계 - 백그라운드에서 가격 정보 업데이트
                        viewModelScope.launch {
                            android.util.Log.w("StockListViewModel", "🚀 백그라운드 가격 업데이트 시작")
                            val stocksWithDayCandles = enrichWithDayCandles(stocksWithCachedData)
                            android.util.Log.w("StockListViewModel", "🔥 일봉 폴백 완료: ${stocksWithDayCandles.count { it.currentPrice > 0 }}개 종목 가격 확보")
                            
                            // 가격 정보 업데이트된 데이터로 UI 재업데이트
                            _uiState.update { currentState ->
                                currentState.copy(
                                    stocks = stocksWithDayCandles,
                                    filteredStocks = applyFiltersAndSort(stocksWithDayCandles)
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "데이터를 불러오는데 실패했습니다"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getSortType(): String {
        // API 정렬 파라미터 반환 (필요시)
        return "code"
    }

    fun loadMoreStocks() {
        val currentState = _uiState.value
        if (!currentState.isLoading && currentState.hasMoreData) {
            _uiState.update { it.copy(currentPage = it.currentPage + 1) }
            loadStocks()
        }
    }

    fun refreshStocks() {
        loadStocks(isRefresh = true)
    }

    fun onTabChange(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
        refreshStocks() // 탭 변경 시 새로운 데이터 로드
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        // 로컬 데이터에서 검색 필터링
        filterStocks()
    }

    private fun searchStocks(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            stockListRepository.searchStocks(
                query = query,
                page = 0,
                size = 50
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val stockListPage = resource.data!!
                        _uiState.update {
                            it.copy(
                                stocks = stockListPage.content,
                                filteredStocks = stockListPage.content,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                        filterStocks()
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "데이터를 불러오는데 실패했습니다"
                            )
                        }
                    }
                }
            }
        }
    }

    fun onFilterChange(filter: String) {
        when (filter) {
            "이름", "현재가", "등락률" -> {
                // 정렬 처리
                handleSorting(filter)
            }
            "관심목록" -> {
                // 관심목록 필터 토글
                _uiState.update { state ->
                    state.copy(showFavoritesOnly = !state.showFavoritesOnly)
                }
                filterStocks()
            }
            else -> {
                // 기타 필터 처리
                _uiState.update { state ->
                    val newFilters = if (state.selectedFilters.contains(filter)) {
                        state.selectedFilters - filter
                    } else {
                        state.selectedFilters + filter
                    }
                    state.copy(selectedFilters = newFilters)
                }
                filterStocks()
            }
        }
    }
    
    private fun handleSorting(field: String) {
        _uiState.update { state ->
            val currentSort = state.currentSortType
            val newSortType = when (field) {
                "이름" -> {
                    when (currentSort) {
                        SortType.NAME_ASC -> SortType.NAME_DESC
                        SortType.NAME_DESC -> SortType.NAME_ASC
                        else -> SortType.NAME_ASC  // 첫 클릭: 오름차순
                    }
                }
                "현재가" -> {
                    when (currentSort) {
                        SortType.PRICE_DESC -> SortType.PRICE_ASC
                        SortType.PRICE_ASC -> SortType.PRICE_DESC
                        else -> SortType.PRICE_DESC  // 첫 클릭: 내림차순
                    }
                }
                "등락률" -> {
                    when (currentSort) {
                        SortType.CHANGE_DESC -> SortType.CHANGE_ASC
                        SortType.CHANGE_ASC -> SortType.CHANGE_DESC
                        else -> SortType.CHANGE_DESC  // 첫 클릭: 내림차순
                    }
                }
                else -> currentSort
            }
            state.copy(currentSortType = newSortType)
        }
        applySorting()
    }

    fun toggleFavorite(stockCode: String) {
        viewModelScope.launch {
            mockTradeRepository.toggleFavorite(stockCode).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        android.util.Log.d("StockListViewModel", "💖 관심종목 토글 중: $stockCode")
                    }
                    is Resource.Success -> {
                        val action = if (resource.data == true) "추가" else "제거"
                        android.util.Log.d("StockListViewModel", "💖 관심종목 토글 성공: $stockCode → $action")
                    }
                    is Resource.Error -> {
                        android.util.Log.e("StockListViewModel", "💖 관심종목 토글 실패: $stockCode - ${resource.message}")
                        _uiState.update { it.copy(errorMessage = resource.message) }
                    }
                }
            }
        }
    }

    private fun filterStocks() {
        _uiState.update { state ->
            var filtered = state.stocks

            // 검색어 필터
            if (state.searchQuery.isNotEmpty()) {
                val query = state.searchQuery.lowercase()
                filtered = filtered.filter { stock ->
                    stock.name.lowercase().contains(query) || 
                    stock.code.lowercase().contains(query)
                }
            }

            // 관심목록 필터
            if (state.showFavoritesOnly) {
                filtered = filtered.filter { it.isFavorite }
            }

            state.copy(filteredStocks = filtered)
        }
        // 필터링 후 정렬 적용
        applySorting()
    }
    
    private fun applySorting() {
        _uiState.update { state ->
            var sorted = state.filteredStocks
            
            sorted = when (state.currentSortType) {
                SortType.NAME_ASC -> sorted.sortedBy { it.name }
                SortType.NAME_DESC -> sorted.sortedByDescending { it.name }
                SortType.PRICE_ASC -> sorted.sortedBy { it.currentPrice }
                SortType.PRICE_DESC -> sorted.sortedByDescending { it.currentPrice }
                SortType.CHANGE_ASC -> sorted.sortedBy { it.priceChangePercent }
                SortType.CHANGE_DESC -> sorted.sortedByDescending { it.priceChangePercent }
                SortType.NONE -> sorted
            }
            
            state.copy(filteredStocks = sorted)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * 캐시된 실시간 데이터와 API 종목 데이터를 병합
     * API에서 0원으로 온 데이터에 실시간 캐시 데이터 적용 + 관심종목 상태 적용
     */
    private fun mergeWithCachedData(stocks: List<StockItem>): List<StockItem> {
        val favorites = favoriteCache.favoriteFlow.value
        return stocks.map { stock ->
            val cachedData = realTimeCache.getStockData(stock.code)
            val isFavorite = favorites.contains(stock.code)
            
            if (cachedData != null) {
                android.util.Log.d("StockListViewModel", "💾 캐시 적용: ${stock.code} = ${cachedData.price}원 (기존 ${stock.currentPrice}원), 관심종목: $isFavorite")
                stock.copy(
                    currentPrice = cachedData.price.toInt(),
                    priceChange = cachedData.priceChange.toInt(),
                    priceChangePercent = cachedData.priceChangePercent,
                    volume = cachedData.volume ?: 0L,
                    updatedAt = java.time.Instant.ofEpochMilli(cachedData.timestamp).toString(),
                    isFavorite = isFavorite
                )
            } else {
                android.util.Log.v("StockListViewModel", "💾 ${stock.code}: 캐시 데이터 없음, 기본값 유지, 관심종목: $isFavorite")
                stock.copy(isFavorite = isFavorite)
            }
        }
    }

    /**
     * 일봉 데이터에서 최근 2일 종가로 현재가/등락률 계산
     * 웹소켓/캐시 데이터 없을 때 폴백용
     */
    private suspend fun enrichWithDayCandles(stocks: List<StockItem>): List<StockItem> = coroutineScope {
        // 한국 주식시장 영업일 기준으로 날짜 계산
        val (startDate, endDate) = com.lago.app.util.KoreanStockMarketUtils.getChartDateRange()
        com.lago.app.util.KoreanStockMarketUtils.logTradingDayInfo()
        
        // 0원인 종목 개수 확인
        val zeroStocks = stocks.filter { it.currentPrice == 0 }
        android.util.Log.d("StockListViewModel", "📈 병렬 처리 - 일봉 계산 대상: ${zeroStocks.size}개 종목")
        
        // 병렬 처리로 성능 대폭 향상 (40번 순차 → 40번 병렬)
        val results = stocks.map { stock ->
            async {
                // 이미 실시간 데이터가 있거나 가격이 0이 아니면 스킵
                if (stock.currentPrice != 0) {
                    return@async stock
                }
                
                // 모든 종목 처리 (테스트 제한 제거)
                try {
                    // 로그 최소화 (성능 향상)
                    // android.util.Log.d("StockListViewModel", "📈 ${stock.code}: 인터벌 API로 일봉 데이터 계산 시작")
                    
                    // Flow 예외 안전 처리
                    val processedStock = runCatching {
                        
                        // 안전한 Flow 처리
                        var resource: com.lago.app.util.Resource<List<CandlestickData>>? = null
                        
                        try {
                            // 성공하는 인터벌 API 사용 (DAY 간격)
                            val fromDateTime = "${startDate}T09:00:00"
                            val toDateTime = "${endDate}T15:30:00"
                            
                            chartRepository.getIntervalChartData(stock.code, "DAY", fromDateTime, toDateTime)
                                .catch { e ->
                                    resource = com.lago.app.util.Resource.Error("Flow error: ${e.message}")
                                }
                                .collect { res ->
                                    resource = res
                                    if (res is com.lago.app.util.Resource.Success || res is com.lago.app.util.Resource.Error) {
                                        return@collect // 성공 또는 에러 시 collect 중단
                                    }
                                }
                        } catch (e: Exception) {
                            android.util.Log.e("StockListViewModel", "📈 ${stock.code}: collect 예외 - ${e.message}")
                            resource = com.lago.app.util.Resource.Error("Collect error: ${e.message}")
                        }
                        
                        val finalResource = resource ?: com.lago.app.util.Resource.Error("No response")
                        
                        when (finalResource) {
                            is com.lago.app.util.Resource.Success -> {
                                val candles = finalResource.data!!
                                if (candles.size >= 2) {
                                    val latestCandle = candles.last() // 가장 최근일
                                    val previousCandle = candles[candles.size - 2] // 전일
                                    
                                    val currentPrice = latestCandle.close.toInt()
                                    val priceChange = (latestCandle.close - previousCandle.close).toInt()
                                    val priceChangePercent = if (previousCandle.close != 0f) {
                                        ((latestCandle.close - previousCandle.close) / previousCandle.close * 100).toDouble()
                                    } else 0.0
                                    
                                    // 성공 시에만 로그 (성능 향상)
                                    android.util.Log.d("StockListViewModel", "📈 ${stock.code}: ${currentPrice}원 (${if (priceChange >= 0) "+" else ""}${priceChange}원)")
                                    
                                    // 계산된 데이터를 캐시에도 저장
                                    val realTimeData = com.lago.app.domain.entity.StockRealTimeData(
                                        stockCode = stock.code,
                                        closePrice = latestCandle.close.toLong(),
                                        changePrice = priceChange.toLong(),
                                        fluctuationRate = priceChangePercent,
                                        volume = latestCandle.volume?.toLong(),
                                        timestamp = System.currentTimeMillis()
                                    )
                                    realTimeCache.updateStock(stock.code, realTimeData)
                                    
                                    return@runCatching stock.copy(
                                        currentPrice = currentPrice,
                                        priceChange = priceChange,
                                        priceChangePercent = priceChangePercent,
                                        volume = latestCandle.volume?.toLong() ?: 0L,
                                        isFavorite = favoriteCache.favoriteFlow.value.contains(stock.code)
                                    )
                                } else {
                                    android.util.Log.w("StockListViewModel", "📈 ${stock.code}: 일봉 데이터 부족 (${candles.size}개)")
                                }
                            }
                            is com.lago.app.util.Resource.Error -> {
                                // 일봉 데이터 실패는 로그에만 기록 (사용자에게는 숨김)
                                android.util.Log.d("StockListViewModel", "📈 ${stock.code}: 일봉 데이터 조회 실패")
                            }
                            is com.lago.app.util.Resource.Loading -> {
                                android.util.Log.d("StockListViewModel", "📈 ${stock.code}: 일봉 데이터 로딩 중...")
                            }
                        }
                        
                        stock // 기본값 반환
                    }.getOrElse { e ->
                        // 개별 종목 실패는 조용히 처리
                        android.util.Log.d("StockListViewModel", "📈 ${stock.code}: 처리 실패")
                        stock
                    }
                    
                    processedStock
                } catch (e: Exception) {
                    // 예외는 조용히 처리하고 기본값 사용
                    android.util.Log.d("StockListViewModel", "📈 ${stock.code}: 예외 발생")
                    stock
                }
            }
        }
        
        results.awaitAll()
    }

    fun getTrendingStocks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            stockListRepository.getTrendingStocks(20).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val trendingStocks = resource.data!!
                        _uiState.update {
                            it.copy(
                                stocks = trendingStocks,
                                filteredStocks = trendingStocks,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                        applySorting()  // 초기 정렬 적용
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "데이터를 불러오는데 실패했습니다"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadHistoryChallengeStocks() {
        viewModelScope.launch {
            try {
                android.util.Log.d("StockListViewModel", "🔥 역사챌린지 API 호출 시작")
                
                // 실제 /api/history-challenge 엔드포인트 사용 (ChartRepository 통해)
                chartRepository.getHistoryChallenge().collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            android.util.Log.d("StockListViewModel", "🔥 역사챌린지 데이터 로딩 중...")
                        }
                        is Resource.Success -> {
                            val challenge = resource.data ?: return@collect
                            android.util.Log.d("StockListViewModel", "🔥 역사챌린지 성공: ${challenge.stockName} (${challenge.stockCode})")
                            android.util.Log.d("StockListViewModel", "🔥 API 데이터: currentPrice=${challenge.currentPrice}, fluctuationPrice=${challenge.fluctuationPrice}, fluctuationRate=${challenge.fluctuationRate}")
                            
                            // HistoryChallengeResponse를 HistoryChallengeStock으로 변환
                            val historyChallengeStock = HistoryChallengeStock(
                                challengeId = challenge.challengeId,
                                stockCode = challenge.stockCode,
                                stockName = challenge.stockName,
                                currentPrice = challenge.currentPrice.toFloat(),
                                openPrice = 0f, // WebSocket에서 업데이트
                                highPrice = 0f, // WebSocket에서 업데이트
                                lowPrice = 0f, // WebSocket에서 업데이트
                                closePrice = challenge.currentPrice.toFloat(),
                                fluctuationRate = challenge.fluctuationRate,
                                changePrice = challenge.fluctuationPrice.toFloat(), // API에서 받은 실제 전일대비 가격차이
                                tradingVolume = 0L, // WebSocket에서 업데이트
                                marketCap = null,
                                profitRate = null // 역사챌린지에서는 수익률 별도 계산
                            )
                            
                            _uiState.update {
                                it.copy(historyChallengeStocks = listOf(historyChallengeStock))
                            }
                            
                            android.util.Log.d("StockListViewModel", "🔥 UI 상태 업데이트 완료: ${historyChallengeStock.stockName}")
                            android.util.Log.d("StockListViewModel", "🔥 생성된 HistoryChallengeStock: currentPrice=${historyChallengeStock.currentPrice}, changePrice=${historyChallengeStock.changePrice}, fluctuationRate=${historyChallengeStock.fluctuationRate}")
                            android.util.Log.d("StockListViewModel", "🔥 현재 historyChallengeStocks 크기: ${_uiState.value.historyChallengeStocks.size}")
                            
                            // 역사챌린지 WebSocket 구독 시작 (실제 stockCode 전달)
                            android.util.Log.d("StockListViewModel", "🔥 WebSocket 구독 시작 전: ${challenge.stockCode}")
                            subscribeToHistoryChallengeWebSocket(challenge.stockCode)
                            android.util.Log.d("StockListViewModel", "🔥 WebSocket 구독 시작 후: ${challenge.stockCode}")
                        }
                        is Resource.Error -> {
                            android.util.Log.e("StockListViewModel", "🚨 역사챌린지 API 오류: ${resource.message}")
                            _uiState.update {
                                it.copy(errorMessage = resource.message)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("StockListViewModel", "🚨 역사챌린지 로드 예외", e)
                _uiState.update {
                    it.copy(errorMessage = "역사챌린지 데이터 로드 실패: ${e.message}")
                }
            }
        }
    }

    /**
     * 역사챌린지 WebSocket 구독
     * /topic/history-challenge 채널로 실시간 데이터 수신
     */
    private fun subscribeToHistoryChallengeWebSocket(stockCode: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("StockListViewModel", "🔥 역사챌린지 WebSocket 구독 시작: $stockCode")
                
                // 역사챌린지 WebSocket 구독 시작 (실제 stockCode 전달)
                android.util.Log.d("StockListViewModel", "🔥 역사챌린지 WebSocket 구독 시작: /topic/history-challenge (종목: $stockCode)")
                smartWebSocketService.subscribeToHistoryChallenge(stockCode)
                
            } catch (e: Exception) {
                android.util.Log.e("StockListViewModel", "🚨 역사챌린지 WebSocket 구독 실패", e)
            }
        }
    }
    
    // 주기적 캐시 체크는 더 이상 필요 없음 (StateFlow가 자동 처리)
    
    /**
     * 역사챌린지 실시간 데이터 업데이트
     * WebSocket에서 수신한 데이터로 historyChallengeStocks 업데이트
     */
    private fun updateHistoryChallengeWithRealTimeData(realTimeData: com.lago.app.domain.entity.StockRealTimeData) {
        _uiState.update { currentState ->
            val updatedHistoryStocks = currentState.historyChallengeStocks.map { historyStock ->
                if (historyStock.stockCode == realTimeData.stockCode) {
                    historyStock.copy(
                        currentPrice = realTimeData.closePrice?.toFloat() ?: historyStock.currentPrice,
                        openPrice = realTimeData.openPrice?.toFloat() ?: historyStock.openPrice,
                        highPrice = realTimeData.highPrice?.toFloat() ?: historyStock.highPrice,
                        lowPrice = realTimeData.lowPrice?.toFloat() ?: historyStock.lowPrice,
                        closePrice = realTimeData.closePrice?.toFloat() ?: historyStock.closePrice,
                        fluctuationRate = realTimeData.fluctuationRate?.toFloat() ?: historyStock.fluctuationRate,
                        tradingVolume = realTimeData.volume ?: historyStock.tradingVolume
                    )
                } else {
                    historyStock
                }
            }
            
            currentState.copy(historyChallengeStocks = updatedHistoryStocks)
        }
        
        android.util.Log.d("StockListViewModel", "🔥 역사챌린지 실시간 데이터 업데이트: ${realTimeData.stockCode} = ${realTimeData.closePrice}원")
    }
    
    /**
     * 역사챌린지 종목을 캐시 데이터로 업데이트
     */
    private fun updateHistoryChallengeStocksWithCache(
        historyChallengeStocks: List<HistoryChallengeStock>,
        quotesMap: Map<String, com.lago.app.domain.entity.StockRealTimeData>
    ): List<HistoryChallengeStock> {
        var updateCount = 0
        
        val updatedStocks = historyChallengeStocks.map { stock ->
            val historyChallengeKey = "HISTORY_CHALLENGE_${stock.stockCode}"
            val realTimeData = quotesMap[historyChallengeKey]
            if (realTimeData != null) {
                val newCurrentPrice = realTimeData.closePrice?.toFloat() ?: stock.currentPrice
                val newChangePrice = realTimeData.priceChange.toFloat()
                val newFluctuationRate = realTimeData.priceChangePercent.toFloat()
                
                // 변경 사항이 있는지 체크
                if (stock.currentPrice != newCurrentPrice || 
                    stock.changePrice != newChangePrice || 
                    stock.fluctuationRate != newFluctuationRate) {
                    
                    android.util.Log.d("StockListViewModel", "🔥 역사챌린지 ${stock.stockCode} 실시간 업데이트: ${realTimeData.closePrice}원")
                    android.util.Log.d("StockListViewModel", "🔥 역사챌린지 이전 값: currentPrice=${stock.currentPrice}, changePrice=${stock.changePrice}, fluctuationRate=${stock.fluctuationRate}")
                    android.util.Log.d("StockListViewModel", "🔥 역사챌린지 새 값: currentPrice=${newCurrentPrice}, changePrice=${newChangePrice}, fluctuationRate=${newFluctuationRate}")
                    updateCount++
                    
                    val updatedStock = stock.copy(
                        currentPrice = newCurrentPrice,
                        openPrice = realTimeData.openPrice?.toFloat() ?: stock.openPrice,
                        highPrice = realTimeData.highPrice?.toFloat() ?: stock.highPrice,
                        lowPrice = realTimeData.lowPrice?.toFloat() ?: stock.lowPrice,
                        closePrice = realTimeData.closePrice?.toFloat() ?: stock.closePrice,
                        fluctuationRate = newFluctuationRate,
                        changePrice = newChangePrice,
                        tradingVolume = realTimeData.volume ?: stock.tradingVolume
                    )
                    android.util.Log.d("StockListViewModel", "🔥 역사챌린지 업데이트 후: currentPrice=${updatedStock.currentPrice}, changePrice=${updatedStock.changePrice}, fluctuationRate=${updatedStock.fluctuationRate}")
                    updatedStock
                } else {
                    android.util.Log.v("StockListViewModel", "🔥 역사챌린지 ${stock.stockCode}: 변경사항 없음")
                    stock
                }
            } else {
                android.util.Log.d("StockListViewModel", "🔥 역사챌린지 ${stock.stockCode} (키: $historyChallengeKey): 캐시에 데이터 없음")
                stock
            }
        }
        
        if (updateCount > 0) {
            android.util.Log.d("StockListViewModel", "🔥 역사챌린지 ${updateCount}개 종목 실시간 업데이트 완료")
        }
        
        return updatedStocks
    }
    
    // =====================================
    // 관심종목 기능
    // =====================================
    
    /**
     * 관심종목 상태 변화 관찰
     */
    private fun observeFavoriteData() {
        viewModelScope.launch {
            favoriteCache.favoriteFlow.collect { favorites ->
                // 관심종목 상태가 변경되면 StockItem의 isFavorite 상태 업데이트
                _uiState.update { currentState ->
                    val updatedStocks = currentState.stocks.map { stock ->
                        stock.copy(isFavorite = favorites.contains(stock.code))
                    }
                    currentState.copy(
                        stocks = updatedStocks,
                        filteredStocks = applyFiltersAndSort(updatedStocks)
                    )
                }
                android.util.Log.d("StockListViewModel", "💖 관심종목 상태 업데이트: ${favorites.size}개")
            }
        }
    }
    
    /**
     * 사용자 관심종목 초기 로드
     */
    private fun loadUserFavorites() {
        viewModelScope.launch {
            mockTradeRepository.loadUserFavorites().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        android.util.Log.d("StockListViewModel", "💖 관심종목 로딩 중...")
                    }
                    is Resource.Success -> {
                        android.util.Log.d("StockListViewModel", "💖 관심종목 로드 성공: ${resource.data?.size ?: 0}개")
                        // 캐시는 이미 Repository에서 업데이트됨
                    }
                    is Resource.Error -> {
                        android.util.Log.e("StockListViewModel", "💖 관심종목 로드 실패: ${resource.message}")
                    }
                }
            }
        }
    }
    
    
}
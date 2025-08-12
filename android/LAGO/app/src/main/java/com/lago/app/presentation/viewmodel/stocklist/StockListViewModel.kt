package com.lago.app.presentation.viewmodel.stocklist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.StockItem
import com.lago.app.domain.entity.HistoryChallengeStock
import com.lago.app.domain.repository.StockListRepository
import com.lago.app.domain.repository.HistoryChallengeRepository
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
import kotlinx.coroutines.launch
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
    private val smartWebSocketService: SmartStockWebSocketService,
    private val smartUpdateScheduler: SmartUpdateScheduler,
    private val realTimeCache: com.lago.app.data.cache.RealTimeStockCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockListUiState())
    val uiState: StateFlow<StockListUiState> = _uiState

    init {
        // 1. WebSocket 연결
        smartWebSocketService.connect()
        
        // 2. 캐시 StateFlow 구독 (자동 UI 업데이트)
        observeRealTimeData()
        
        // 3. 초기 데이터 로드
        loadStocks()
        loadHistoryChallengeStocks()
        
        // 4. 테스트용 - 장시간 외 테스트 데이터 (필요시 주석 해제)
        testRealTimeData() // 테스트 활성화!
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
                    android.util.Log.v("StockListViewModel", "🔍 ${stock.code}: 실시간 데이터 없음")
                    stock
                }
            }
            
            // 역사적 챌린지 종목도 업데이트
            var historyUpdateCount = 0
            val updatedHistoryStocks = currentState.historyChallengeStocks.map { historyStock ->
                val realTimeData = quotesMap[historyStock.stockCode]
                if (realTimeData != null && 
                    historyStock.currentPrice != realTimeData.price.toFloat()) {
                    
                    val newPrice = realTimeData.price.toFloat()
                    val basePrice = historyStock.openPrice
                    val newFluctuationRate = if (basePrice > 0) {
                        ((newPrice - basePrice) / basePrice) * 100
                    } else 0f
                    
                    android.util.Log.d("StockListViewModel", "📈 역사 챌린지 ${historyStock.stockCode}: ${historyStock.currentPrice.toInt()}→${newPrice.toInt()}원 (변동률: ${String.format("%.2f", newFluctuationRate)}%)")
                    historyUpdateCount++
                    
                    historyStock.copy(
                        currentPrice = newPrice,
                        fluctuationRate = newFluctuationRate
                    )
                } else {
                    historyStock
                }
            }
            
            android.util.Log.d("StockListViewModel", "✅ UI 업데이트 완료: 일반 ${updateCount}개, 역사챌린지 ${historyUpdateCount}개 종목 변경됨")
            
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
                        newStocks.forEach { stock ->
                            android.util.Log.d("StockListViewModel", "📈 로딩된 종목: ${stock.code} (${stock.name}) = ${stock.currentPrice}원")
                        }
                        
                        // 🎯 핵심: 캐시된 실시간 데이터와 병합
                        val stocksWithRealTimeData = mergeWithCachedData(newStocks)
                        android.util.Log.w("StockListViewModel", "🔥 캐시 병합 완료: ${stocksWithRealTimeData.count { it.currentPrice > 0 }}개 실시간 가격 적용")
                        
                        _uiState.update {
                            it.copy(
                                stocks = stocksWithRealTimeData,
                                filteredStocks = stocksWithRealTimeData,
                                isLoading = false,
                                errorMessage = null,
                                currentPage = stockListPage.page,
                                hasMoreData = stockListPage.page < stockListPage.totalPages - 1
                            )
                        }
                        filterStocks()
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
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
                                errorMessage = resource.message
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
            stockListRepository.toggleFavorite(stockCode).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        // 성공 시 UI 상태 업데이트
                        _uiState.update { state ->
                            val updatedStocks = state.stocks.map { stock ->
                                if (stock.code == stockCode) {
                                    stock.copy(isFavorite = !stock.isFavorite)
                                } else {
                                    stock
                                }
                            }
                            state.copy(stocks = updatedStocks)
                        }
                        filterStocks()
                    }
                    is Resource.Error -> {
                        // 에러 처리 (토스트 메시지 등)
                        _uiState.update { it.copy(errorMessage = resource.message) }
                    }
                    is Resource.Loading -> {
                        // 로딩 상태 처리 (필요시)
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
     * API에서 0원으로 온 데이터에 실시간 캐시 데이터 적용
     */
    private fun mergeWithCachedData(stocks: List<StockItem>): List<StockItem> {
        return stocks.map { stock ->
            val cachedData = realTimeCache.getStockData(stock.code)
            if (cachedData != null && stock.currentPrice == 0) {
                android.util.Log.d("StockListViewModel", "💾 캐시 적용: ${stock.code} = ${cachedData.price}원 (기존 0원)")
                stock.copy(
                    currentPrice = cachedData.price.toInt(),
                    priceChange = cachedData.priceChange.toInt(),
                    priceChangePercent = cachedData.priceChangePercent,
                    volume = cachedData.volume ?: 0L,
                    updatedAt = java.time.Instant.ofEpochMilli(cachedData.timestamp).toString()
                )
            } else {
                stock
            }
        }
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
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadHistoryChallengeStocks() {
        viewModelScope.launch {
            historyChallengeRepository.getHistoryChallengeStocks().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // 로딩 처리는 필요시 추가
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(historyChallengeStocks = resource.data ?: emptyList())
                        }
                    }
                    is Resource.Error -> {
                        // 에러 시 Mock 데이터 사용
                        loadMockHistoryChallengeStocks()
                    }
                }
            }
        }
    }

    private fun loadMockHistoryChallengeStocks() {
        // 역사 챌린지는 1개 종목만 표시
        val mockStocks = listOf(
            HistoryChallengeStock(
                challengeId = 1,
                stockCode = "005930",
                stockName = "삼성전자",
                currentPrice = 74200f,
                openPrice = 73400f,
                highPrice = 75000f,
                lowPrice = 73000f,
                closePrice = 74200f,
                fluctuationRate = 2.14f,
                tradingVolume = 15000000L,
                marketCap = 445000000000000L,
                profitRate = 12.5f
            )
        )
        
        _uiState.update {
            it.copy(historyChallengeStocks = mockStocks)
        }
    }
    
    // 주기적 캐시 체크는 더 이상 필요 없음 (StateFlow가 자동 처리)
    
    /**
     * 테스트용 - 장시간 외에 실시간 데이터 테스트
     * 실제 운영시에는 주석 처리
     */
    private fun testRealTimeData() {
        android.util.Log.w("StockListViewModel", "🚀 테스트 실시간 데이터 생성 시작!")
        viewModelScope.launch {
            delay(2000) // 2초 후 테스트 데이터 주입
            android.util.Log.w("StockListViewModel", "⏰ 2초 대기 완료, 테스트 데이터 주입 시작")
            
            // 여러 종목 테스트 데이터
            val testStocks = listOf(
                Triple("005930", "삼성전자", 75000L),
                Triple("000660", "SK하이닉스", 135000L),
                Triple("035420", "NAVER", 220000L),
                Triple("035720", "카카오", 45000L),
                Triple("207940", "삼성바이오로직스", 1020000L),
                Triple("373220", "LG에너지솔루션", 450000L),
                Triple("051910", "LG화학", 480000L),
                Triple("006400", "삼성SDI", 430000L)
            )
            
            // 초기 데이터 주입
            testStocks.forEach { (code, name, basePrice) ->
                val testData = com.lago.app.domain.entity.StockRealTimeData(
                    stockCode = code,
                    closePrice = basePrice + (Math.random() * 1000).toLong(),
                    openPrice = basePrice,
                    highPrice = basePrice + 2000L,
                    lowPrice = basePrice - 2000L,
                    volume = (Math.random() * 10000000).toLong(),
                    changePrice = (Math.random() * 2000 - 1000).toLong(),
                    changeRate = Math.random() * 4 - 2, // -2% ~ +2%
                    timestamp = System.currentTimeMillis()
                )
                realTimeCache.updateStock(testData.stockCode, testData)
                android.util.Log.w("StockListViewModel", "🧪 테스트 데이터 주입: $name($code) = ${testData.price}원")
            }
            
            // 1초마다 랜덤하게 종목 가격 변동
            while (true) {
                delay(1000)
                
                // 랜덤하게 2-3개 종목 선택하여 업데이트
                val updateCount = (2..3).random()
                val selectedStocks = testStocks.shuffled().take(updateCount)
                android.util.Log.d("StockListViewModel", "🎯 ${updateCount}개 종목 업데이트: ${selectedStocks.map { it.second }.joinToString(", ")}")
                
                selectedStocks.forEach { (code, name, basePrice) ->
                    val currentData = realTimeCache.getStockData(code)
                    val newPrice = if (currentData != null) {
                        // 현재 가격에서 -1% ~ +1% 변동
                        val change = (currentData.closePrice ?: basePrice) * (Math.random() * 0.02 - 0.01)
                        (currentData.closePrice ?: basePrice) + change.toLong()
                    } else {
                        basePrice + (Math.random() * 2000 - 1000).toLong()
                    }
                    
                    val updatedData = com.lago.app.domain.entity.StockRealTimeData(
                        stockCode = code,
                        closePrice = newPrice,
                        openPrice = basePrice,
                        highPrice = maxOf(newPrice, basePrice + 2000L),
                        lowPrice = minOf(newPrice, basePrice - 2000L),
                        volume = (Math.random() * 10000000).toLong(),
                        changePrice = newPrice - basePrice,
                        changeRate = ((newPrice - basePrice).toDouble() / basePrice) * 100,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    realTimeCache.updateStock(code, updatedData)
                    android.util.Log.d("StockListViewModel", "💹 $name: ${updatedData.price.toInt()}원 (${String.format("%+.2f%%", updatedData.changeRate)})")
                }
            }
        }
    }
}
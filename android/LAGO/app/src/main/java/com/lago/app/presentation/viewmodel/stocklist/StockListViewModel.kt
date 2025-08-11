package com.lago.app.presentation.viewmodel.stocklist

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

@HiltViewModel
class StockListViewModel @Inject constructor(
    private val stockListRepository: StockListRepository,
    private val historyChallengeRepository: HistoryChallengeRepository,
    private val smartWebSocketService: SmartStockWebSocketService,
    private val smartUpdateScheduler: SmartUpdateScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockListUiState())
    val uiState: StateFlow<StockListUiState> = _uiState

    init {
        loadStocks()
        loadHistoryChallengeStocks()
        setupRealTimeUpdates()
    }
    
    private fun setupRealTimeUpdates() {
        viewModelScope.launch {
            // 종목리스트용 실시간 업데이트 구독
            smartUpdateScheduler.stockListUpdates.collect { updates ->
                updateStocksWithRealTimeData(updates)
            }
        }
    }
    
    private fun updateStocksWithRealTimeData(updates: Map<String, com.lago.app.domain.entity.StockRealTimeData>) {
        _uiState.update { currentState ->
            val updatedStocks = currentState.stocks.map { stock ->
                val realTimeData = updates[stock.code]
                if (realTimeData != null) {
                    stock.copy(
                        currentPrice = realTimeData.currentPrice.toInt(),
                        priceChange = realTimeData.priceChange.toInt(),
                        priceChangePercent = realTimeData.priceChangePercent,
                        volume = realTimeData.volume,
                        updatedAt = java.time.Instant.ofEpochMilli(realTimeData.timestamp).toString()
                    )
                } else {
                    stock
                }
            }
            
            currentState.copy(
                stocks = updatedStocks,
                filteredStocks = applyFiltersAndSort(updatedStocks)
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
                        
                        _uiState.update {
                            it.copy(
                                stocks = newStocks,
                                filteredStocks = newStocks,
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
}
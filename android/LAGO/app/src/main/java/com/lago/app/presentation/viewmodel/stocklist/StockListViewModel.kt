package com.lago.app.presentation.viewmodel.stocklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.StockItem
import com.lago.app.domain.repository.StockListRepository
import com.lago.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockListUiState(
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val selectedFilters: List<String> = emptyList(),
    val stocks: List<StockItem> = emptyList(),
    val filteredStocks: List<StockItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentPage: Int = 0,
    val hasMoreData: Boolean = true
)

@HiltViewModel
class StockListViewModel @Inject constructor(
    private val stockListRepository: StockListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockListUiState())
    val uiState: StateFlow<StockListUiState> = _uiState

    init {
        loadStocks()
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
        val filters = _uiState.value.selectedFilters
        return when {
            filters.contains("이름") -> "name"
            filters.contains("현재가") -> "price"
            filters.contains("등락률") -> "changeRate"
            else -> "code"
        }
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
        
        if (query.isNotEmpty()) {
            // 검색 API 호출
            searchStocks(query)
        } else {
            // 검색어가 비어있으면 기본 목록으로 복원
            refreshStocks()
        }
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
        _uiState.update { state ->
            val newFilters = if (state.selectedFilters.contains(filter)) {
                state.selectedFilters - filter
            } else {
                state.selectedFilters + filter
            }
            state.copy(selectedFilters = newFilters)
        }
        
        // 정렬 필터가 변경된 경우 새로운 데이터 로드
        if (filter in listOf("이름", "현재가", "등락률")) {
            refreshStocks()
        } else {
            filterStocks()
        }
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

            // 관심목록 필터 (검색은 이미 API에서 처리됨)
            if (state.selectedFilters.contains("관심목록")) {
                filtered = filtered.filter { it.isFavorite }
            }

            // 로컬 정렬 (API 정렬과 함께 사용)
            filtered = when {
                state.selectedFilters.contains("이름") -> filtered.sortedBy { it.name }
                state.selectedFilters.contains("현재가") -> filtered.sortedByDescending { it.currentPrice }
                state.selectedFilters.contains("등락률") -> filtered.sortedByDescending { it.priceChangePercent }
                else -> filtered
            }

            state.copy(filteredStocks = filtered)
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
}
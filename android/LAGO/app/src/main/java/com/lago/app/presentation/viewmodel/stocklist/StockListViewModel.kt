package com.lago.app.presentation.viewmodel.stocklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockItem(
    val code: String,
    val name: String,
    val currentPrice: Int,
    val change: Int,
    val changeRate: Double,
    val isFavorite: Boolean = false
)

data class StockListUiState(
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val selectedFilters: List<String> = emptyList(),
    val stocks: List<StockItem> = emptyList(),
    val filteredStocks: List<StockItem> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class StockListViewModel @Inject constructor(
    // repository 주입
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockListUiState())
    val uiState: StateFlow<StockListUiState> = _uiState

    init {
        loadStocks()
    }

    private fun loadStocks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // API 호출 또는 로컬 데이터 로드
            val stocks = listOf(
                StockItem("005930", "삼성전자", 71500, 1500, 2.14, true),
                StockItem("000660", "SK하이닉스", 125000, -1500, -1.19),
                StockItem("035420", "NAVER", 180000, 2500, 1.41),
                StockItem("051910", "LG화학", 445000, -5000, -1.11),
                StockItem("006400", "삼성SDI", 750000, 15000, 2.04, true),
                StockItem("035720", "카카오", 95000, -2000, -2.06),
                StockItem("207940", "삼성바이오로직스", 820000, 10000, 1.23)
            )

            _uiState.update {
                it.copy(
                    stocks = stocks,
                    filteredStocks = stocks,
                    isLoading = false
                )
            }
        }
    }

    fun onTabChange(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
        // 탭에 따라 다른 데이터 로드
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterStocks()
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
        filterStocks()
    }

    fun toggleFavorite(stockCode: String) {
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

    private fun filterStocks() {
        _uiState.update { state ->
            var filtered = state.stocks

            // 검색어 필터
            if (state.searchQuery.isNotEmpty()) {
                filtered = filtered.filter {
                    it.name.contains(state.searchQuery, ignoreCase = true)
                }
            }

            // 관심목록 필터
            if (state.selectedFilters.contains("관심목록")) {
                filtered = filtered.filter { it.isFavorite }
            }

            // 정렬
            filtered = when {
                state.selectedFilters.contains("이름") -> filtered.sortedBy { it.name }
                state.selectedFilters.contains("현재가") -> filtered.sortedByDescending { it.currentPrice }
                state.selectedFilters.contains("등락률") -> filtered.sortedByDescending { it.changeRate }
                else -> filtered
            }

            state.copy(filteredStocks = filtered)
        }
    }
}
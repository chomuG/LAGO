package com.lago.app.presentation.viewmodel.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.data.cache.RealTimeStockCache
import com.lago.app.domain.entity.AccountBalance
import com.lago.app.domain.entity.StockHolding
import com.lago.app.domain.entity.StockPriority
import com.lago.app.domain.repository.MockTradeRepository
import com.lago.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 포트폴리오 UI 상태
 */
data class PortfolioUiState(
    val accountBalance: AccountBalance? = null,
    val stockHoldings: List<StockHolding> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalProfitLoss: Long = 0L,
    val totalProfitLossRate: Double = 0.0,
    val refreshing: Boolean = false
)

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val mockTradeRepository: MockTradeRepository,
    private val realTimeStockCache: RealTimeStockCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState

    init {
        loadPortfolio()
        observeRealTimeUpdates()
    }

    /**
     * 포트폴리오 전체 데이터 로드
     */
    fun loadPortfolio() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // 1. 계좌 잔고 조회
                mockTradeRepository.getAccountBalance().collect { balanceResource ->
                    when (balanceResource) {
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is Resource.Success -> {
                            val accountBalance = balanceResource.data!!
                            _uiState.update { 
                                it.copy(
                                    accountBalance = accountBalance,
                                    isLoading = false
                                )
                            }
                            
                            // 2. 보유 주식 조회
                            loadStockHoldings()
                        }
                        is Resource.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    errorMessage = balanceResource.message
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "포트폴리오 로드 중 오류가 발생했습니다: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * 보유 주식 목록 조회
     */
    private suspend fun loadStockHoldings() {
        mockTradeRepository.getStockHoldings().collect { holdingsResource ->
            when (holdingsResource) {
                is Resource.Loading -> {
                    // 이미 로딩 중
                }
                is Resource.Success -> {
                    val holdings = holdingsResource.data!!
                    
                    // 보유 종목들을 WARM 우선순위로 설정 (실시간 업데이트)
                    val stockPriorities = holdings.associate { it.stockCode to StockPriority.WARM }
                    realTimeStockCache.setMultipleStockPriorities(stockPriorities)
                    
                    android.util.Log.d("PortfolioViewModel", "📈 보유 종목 실시간 우선순위 설정: ${holdings.size}개")
                    
                    updatePortfolioWithCurrentData(holdings)
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = holdingsResource.message
                        )
                    }
                }
            }
        }
    }

    /**
     * 새로고침
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(refreshing = true) }
            loadPortfolio()
            _uiState.update { it.copy(refreshing = false) }
        }
    }

    /**
     * 계좌 리셋 (모든 보유 주식 및 거래내역 삭제, 초기 자금으로 리셋)
     */
    fun resetAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            mockTradeRepository.resetAccount().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        // 리셋 완료 후 포트폴리오 다시 로드
                        loadPortfolio()
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

    /**
     * 계좌 초기화 (신규 가입시)
     */
    fun initializeAccount(initialBalance: Long = 10000000L) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            mockTradeRepository.initializeAccount(initialBalance).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        // 초기화 완료 후 포트폴리오 다시 로드
                        loadPortfolio()
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

    /**
     * 실시간 데이터 업데이트 감시
     */
    private fun observeRealTimeUpdates() {
        viewModelScope.launch {
            // 500ms마다 실시간 데이터 업데이트 체크 (UI 성능 고려)
            realTimeStockCache.quotes
                .sample(500L) // UI 업데이트 쓰로틀링
                .collect { quotesMap ->
                    val currentState = _uiState.value
                    if (currentState.stockHoldings.isNotEmpty()) {
                        updatePortfolioWithRealtimeData(currentState.stockHoldings, quotesMap)
                    }
                }
        }
    }

    /**
     * 보유 주식 데이터를 현재 가격으로 업데이트
     */
    private fun updatePortfolioWithCurrentData(originalHoldings: List<StockHolding>) {
        val quotesMap = realTimeStockCache.quotes.value
        updatePortfolioWithRealtimeData(originalHoldings, quotesMap)
    }

    /**
     * 실시간 데이터로 포트폴리오 업데이트
     */
    private fun updatePortfolioWithRealtimeData(
        originalHoldings: List<StockHolding>,
        quotesMap: Map<String, com.lago.app.domain.entity.StockRealTimeData>
    ) {
        android.util.Log.v("PortfolioViewModel", "💹 실시간 포트폴리오 업데이트 - 보유종목: ${originalHoldings.size}개, 실시간데이터: ${quotesMap.size}개")
        
        val updatedHoldings = originalHoldings.map { holding ->
            val realTimeData = quotesMap[holding.stockCode]
            if (realTimeData != null && holding.currentPrice != realTimeData.price.toInt()) {
                val newCurrentPrice = realTimeData.price.toInt()
                val newCurrentValue = holding.quantity.toLong() * newCurrentPrice
                val newProfitLoss = newCurrentValue - holding.totalBuyAmount
                val newProfitLossRate = if (holding.totalBuyAmount > 0) {
                    (newProfitLoss.toDouble() / holding.totalBuyAmount) * 100
                } else 0.0
                
                android.util.Log.d("PortfolioViewModel", "📊 ${holding.stockCode} 실시간 업데이트: ${holding.currentPrice}원 → ${newCurrentPrice}원")
                
                holding.copy(
                    currentPrice = newCurrentPrice,
                    currentValue = newCurrentValue,
                    profitLoss = newProfitLoss,
                    profitLossRate = newProfitLossRate
                )
            } else {
                holding
            }
        }
        
        // 포트폴리오 전체 손익 재계산
        val totalProfitLoss = updatedHoldings.sumOf { it.profitLoss }
        val totalInvestment = updatedHoldings.sumOf { it.totalBuyAmount }
        val totalProfitLossRate = if (totalInvestment > 0) {
            (totalProfitLoss.toDouble() / totalInvestment) * 100
        } else 0.0
        
        _uiState.update { 
            it.copy(
                stockHoldings = updatedHoldings,
                totalProfitLoss = totalProfitLoss,
                totalProfitLossRate = totalProfitLossRate,
                isLoading = false
            )
        }
    }

    /**
     * 에러 메시지 클리어
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 개별 종목의 현재 수익률 계산
     */
    fun calculateProfitLossRate(holding: StockHolding): Double {
        return if (holding.totalBuyAmount > 0) {
            (holding.profitLoss.toDouble() / holding.totalBuyAmount) * 100
        } else 0.0
    }

    /**
     * 개별 종목의 현재 평가액 계산
     */
    fun calculateCurrentValue(holding: StockHolding): Long {
        return holding.quantity.toLong() * holding.currentPrice
    }

    /**
     * 포트폴리오 분산도 계산 (각 종목이 포트폴리오에서 차지하는 비중)
     */
    fun calculatePortfolioWeight(holding: StockHolding): Double {
        val state = _uiState.value
        val totalValue = state.stockHoldings.sumOf { it.currentValue }
        
        return if (totalValue > 0) {
            (holding.currentValue.toDouble() / totalValue) * 100
        } else 0.0
    }
}
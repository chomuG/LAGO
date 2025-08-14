package com.lago.app.presentation.viewmodel.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.StockDisplayInfo
import com.lago.app.domain.entity.StockHolding
import com.lago.app.domain.entity.AccountBalance
import com.lago.app.domain.entity.MockTradeResult
import com.lago.app.domain.repository.MockTradeRepository
import com.lago.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PurchaseUiState(
    val stockCode: String = "",
    val stockName: String = "",
    val currentPrice: Int = 0,
    val holdingInfo: String? = null,
    val purchaseAmount: Long = 0L,
    val purchaseQuantity: Int = 0,
    val totalPrice: Long = 0L,
    val percentage: Float = 0f,
    val maxAmount: Long = 10000000L, // 최대 구매 가능 금액
    val isLoading: Boolean = false,
    val isPurchaseType: Boolean = true, // true: 구매, false: 판매
    val holdingQuantity: Int = 0, // 보유 주식 수
    val accountBalance: Long = 0L, // 계좌 잔고
    val errorMessage: String? = null,
    val isTradeCompleted: Boolean = false,
    val tradeResult: MockTradeResult? = null
)

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val mockTradeRepository: MockTradeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState

    fun loadStockInfo(stockCode: String, isPurchaseType: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // 1. 주식 정보 조회
                mockTradeRepository.getStockDisplayInfo(stockCode).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is Resource.Success -> {
                            val stockInfo = resource.data!!
                            
                            // 2. 계좌 잔고 조회
                            loadAccountInfo(stockInfo, isPurchaseType)
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
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "주식 정보 로드 중 오류가 발생했습니다: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private suspend fun loadAccountInfo(stockInfo: StockDisplayInfo, isPurchaseType: Boolean) {
        // 계좌 잔고 조회
        mockTradeRepository.getAccountBalance().collect { balanceResource ->
            when (balanceResource) {
                is Resource.Success -> {
                    val balance = balanceResource.data!!
                    
                    if (isPurchaseType) {
                        // 매수: 계좌 잔고 기반으로 최대 구매 가능 금액 설정
                        _uiState.update { state ->
                            state.copy(
                                stockCode = stockInfo.code,
                                stockName = stockInfo.name,
                                currentPrice = stockInfo.currentPrice,
                                holdingInfo = "${String.format("%,d", balance.balance)}원",
                                isPurchaseType = isPurchaseType,
                                maxAmount = balance.balance,
                                accountBalance = balance.balance,
                                holdingQuantity = 0,
                                isLoading = false
                            )
                        }
                    } else {
                        // 매도: 보유 주식 수량 조회
                        loadHoldingInfo(stockInfo, balance)
                    }
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = balanceResource.message
                        )
                    }
                }
                is Resource.Loading -> {
                    // 이미 로딩 중
                }
            }
        }
    }

    private suspend fun loadHoldingInfo(stockInfo: StockDisplayInfo, balance: AccountBalance) {
        // 보유 주식 조회
        mockTradeRepository.getStockHoldings().collect { holdingsResource ->
            when (holdingsResource) {
                is Resource.Success -> {
                    val holdings = holdingsResource.data!!
                    val holding = holdings.find { it.stockCode == stockInfo.code }
                    
                    _uiState.update { state ->
                        state.copy(
                            stockCode = stockInfo.code,
                            stockName = stockInfo.name,
                            currentPrice = stockInfo.currentPrice,
                            holdingInfo = if (holding != null) {
                                "${holding.quantity}주 보유 (평균 ${String.format("%,d", holding.avgBuyPrice)}원)"
                            } else {
                                "보유 주식 없음"
                            },
                            isPurchaseType = false,
                            maxAmount = (holding?.quantity ?: 0) * stockInfo.currentPrice.toLong(),
                            accountBalance = balance.balance,
                            holdingQuantity = holding?.quantity ?: 0,
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = holdingsResource.message
                        )
                    }
                }
                is Resource.Loading -> {
                    // 이미 로딩 중
                }
            }
        }
    }

    fun onAmountChange(amount: Long) {
        val state = _uiState.value
        val maxAmount = if (state.isPurchaseType) {
            state.maxAmount
        } else {
            state.holdingQuantity * state.currentPrice.toLong()
        }
        
        val validAmount = amount.coerceIn(0L, maxAmount)
        val quantity = if (state.currentPrice > 0) {
            (validAmount / state.currentPrice).toInt()
        } else 0
        
        val percentage = if (maxAmount > 0) {
            (validAmount.toFloat() / maxAmount * 100f).coerceIn(0f, 100f)
        } else 0f

        _uiState.update {
            it.copy(
                purchaseAmount = validAmount,
                purchaseQuantity = quantity,
                totalPrice = validAmount,
                percentage = percentage
            )
        }
    }

    fun onPercentageClick(percentage: Float) {
        val state = _uiState.value
        val maxAmount = if (state.isPurchaseType) {
            state.maxAmount
        } else {
            state.holdingQuantity * state.currentPrice.toLong()
        }
        
        val amount = (maxAmount * percentage / 100f).toLong()
        onAmountChange(amount)
    }

    /**
     * 매수/매도 주문 실행
     */
    fun executeTrade() {
        val state = _uiState.value
        
        // 입력값 검증
        if (state.purchaseQuantity <= 0) {
            _uiState.update { it.copy(errorMessage = "수량을 입력해주세요") }
            return
        }
        
        if (state.currentPrice <= 0) {
            _uiState.update { it.copy(errorMessage = "유효하지 않은 주식 가격입니다") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                if (state.isPurchaseType) {
                    // 매수 주문
                    executeBuyOrder(state)
                } else {
                    // 매도 주문
                    executeSellOrder(state)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "거래 처리 중 오류가 발생했습니다: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private suspend fun executeBuyOrder(state: PurchaseUiState) {
        // 잔고 확인
        if (state.purchaseAmount > state.accountBalance) {
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    errorMessage = "잔고가 부족합니다"
                )
            }
            return
        }

        mockTradeRepository.buyStock(
            stockCode = state.stockCode,
            quantity = state.purchaseQuantity,
            price = state.currentPrice
        ).collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Resource.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isTradeCompleted = true,
                            tradeResult = resource.data,
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

    private suspend fun executeSellOrder(state: PurchaseUiState) {
        // 보유 수량 확인
        if (state.purchaseQuantity > state.holdingQuantity) {
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    errorMessage = "보유 수량이 부족합니다"
                )
            }
            return
        }

        mockTradeRepository.sellStock(
            stockCode = state.stockCode,
            quantity = state.purchaseQuantity,
            price = state.currentPrice
        ).collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Resource.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isTradeCompleted = true,
                            tradeResult = resource.data,
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

    /**
     * 에러 메시지 클리어
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 거래 완료 상태 리셋
     */
    fun resetTradeState() {
        _uiState.update { 
            it.copy(
                isTradeCompleted = false,
                tradeResult = null,
                purchaseAmount = 0L,
                purchaseQuantity = 0,
                totalPrice = 0L,
                percentage = 0f
            )
        }
    }
}
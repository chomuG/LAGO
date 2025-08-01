package com.lago.app.presentation.viewmodel.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val holdingQuantity: Int = 0 // 보유 주식 수
)

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    // repository 주입
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState

    fun loadStockInfo(stockCode: String, isPurchaseType: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // API 호출로 주식 정보 로드
            val stockInfo = getStockInfo(stockCode)
            val holdingQuantity = if (!isPurchaseType) getHoldingQuantity(stockCode) else 0

            _uiState.update {
                it.copy(
                    stockCode = stockCode,
                    stockName = stockInfo.name,
                    currentPrice = stockInfo.currentPrice,
                    holdingInfo = "1,500만원",
                    isPurchaseType = isPurchaseType,
                    holdingQuantity = holdingQuantity,
                    isLoading = false
                )
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

    fun purchaseStock() {
        viewModelScope.launch {
            val state = _uiState.value
            // API 호출로 주식 구매/판매 처리
            if (state.isPurchaseType) {
                // 구매 처리
            } else {
                // 판매 처리
            }
        }
    }

    private suspend fun getStockInfo(stockCode: String): StockInfo {
        // API 호출 시뮬레이션
        return when (stockCode) {
            "005930" -> StockInfo("삼성전자", 72500)
            "000660" -> StockInfo("SK하이닉스", 125000)
            "035420" -> StockInfo("NAVER", 180000)
            else -> StockInfo("삼성전자", 72500)
        }
    }

    private suspend fun getHoldingQuantity(stockCode: String): Int {
        // API 호출로 보유 주식 수량 조회
        return 2 // 예시로 2주 보유
    }

    data class StockInfo(
        val name: String,
        val currentPrice: Int
    )
}
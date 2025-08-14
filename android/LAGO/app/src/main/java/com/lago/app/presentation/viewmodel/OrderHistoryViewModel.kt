package com.lago.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.Transaction
import com.lago.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()
    
    fun loadTransactions(userId: Long) {
        viewModelScope.launch {
            android.util.Log.d("VIEWMODEL", "OrderHistoryViewModel - Loading transactions for userId: $userId")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            transactionRepository.getTransactions(userId).fold(
                onSuccess = { transactions ->
                    android.util.Log.d("VIEWMODEL", "OrderHistoryViewModel - Success: Loaded ${transactions.size} transactions")
                    transactions.forEachIndexed { index, transaction ->
                        android.util.Log.v("VIEWMODEL", "Transaction $index: ${transaction.stockName} - ${transaction.buySell} - ${transaction.quantity} - ${transaction.price}")
                    }
                    _uiState.value = _uiState.value.copy(
                        transactions = transactions,
                        isLoading = false
                    )
                },
                onFailure = { exception ->
                    android.util.Log.e("VIEWMODEL", "OrderHistoryViewModel - Error loading transactions", exception)
                    android.util.Log.e("VIEWMODEL", "Error message: ${exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "거래 내역을 불러오는 중 오류가 발생했습니다."
                    )
                }
            )
        }
    }
    
    fun getFilteredTransactions(orderType: OrderType, selectedMonth: String): List<Transaction> {
        return _uiState.value.transactions.filter { transaction ->
            val monthMatches = formatMonth(transaction.tradeAt) == selectedMonth
            val typeMatches = when (orderType) {
                OrderType.ALL -> true
                OrderType.BUY -> transaction.buySell == "BUY"
                OrderType.SELL -> transaction.buySell == "SELL"
            }
            monthMatches && typeMatches
        }
    }
    
    fun getAvailableMonths(): List<String> {
        return _uiState.value.transactions
            .map { formatMonth(it.tradeAt) }
            .distinct()
            .sortedDescending()
    }
    
    private fun formatMonth(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return "${calendar.get(Calendar.YEAR)}년 ${calendar.get(Calendar.MONTH) + 1}월"
    }
}

data class OrderHistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class OrderType(val displayName: String) {
    ALL("전체"),
    BUY("구매"),
    SELL("판매")
}
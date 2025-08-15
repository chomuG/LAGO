package com.lago.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.data.local.prefs.UserPreferences
import com.lago.app.domain.entity.Transaction
import com.lago.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()
    
    fun loadTransactions(userId: Int?) {
        viewModelScope.launch {
            android.util.Log.d("VIEWMODEL", "OrderHistoryViewModel - Loading transactions for userId: $userId")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // userId가 null이면 UserPreferences에서 가져오기 시도
            val actualUserId = userId ?: run {
                val storedUserId = userPreferences.getUserId()
                android.util.Log.d("VIEWMODEL", "OrderHistoryViewModel - Using stored userId: $storedUserId")
                storedUserId?.toIntOrNull()
            }
            
            val userIdLong = actualUserId?.toLong() ?: run {
                android.util.Log.e("VIEWMODEL", "OrderHistoryViewModel - No valid userId found")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "사용자 ID가 없습니다. 다시 로그인해주세요."
                )
                return@launch
            }
            
            transactionRepository.getTransactions(userIdLong).fold(
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
    
    // 개발자용 테스트 함수 - 특정 사용자 ID로 강제 로드
    fun loadTransactionsForDeveloper(developerId: Int) {
        android.util.Log.d("VIEWMODEL", "OrderHistoryViewModel - Developer mode: Loading transactions for developerId: $developerId")
        loadTransactions(developerId)
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
package com.lago.app.presentation.viewmodel.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.data.cache.RealTimeStockCache
import com.lago.app.domain.repository.UserRepository
import com.lago.app.data.remote.dto.MyPagePortfolioSummary
import com.lago.app.data.remote.dto.PieChartItem
import com.lago.app.data.remote.dto.UserCurrentStatusDto
import com.lago.app.data.remote.websocket.SmartStockWebSocketService
import com.lago.app.util.PortfolioCalculator
import com.lago.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import javax.inject.Inject

/**
 * 매매봇 전용 ViewModel
 */
@HiltViewModel
class BotPortfolioViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val realTimeStockCache: RealTimeStockCache,
    private val smartWebSocketService: SmartStockWebSocketService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState

    // 캐시된 API 데이터
    private var cachedUserStatus: UserCurrentStatusDto? = null

    init {
        // 매매봇용이므로 init에서는 아무것도 로드하지 않음
        observeRealTimeUpdates()
    }

    /**
     * 매매봇 포트폴리오 로드 (API 호출)
     */
    fun loadBotPortfolio(userId: Int) {
        viewModelScope.launch {
            // 상태 완전 리셋
            cachedUserStatus = null
            _uiState.value = MyPageUiState(isLoading = true)
            
            try {
                android.util.Log.d("BotPortfolioViewModel", "🤖 매매봇 API 요청 시작: userId=$userId")

                userRepository.getUserCurrentStatus(userId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            android.util.Log.d("BotPortfolioViewModel", "⏳ 매매봇 API 로딩 중...")
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is Resource.Success -> {
                            android.util.Log.d("BotPortfolioViewModel", "✅ 매매봇 API 성공: ${resource.data}")
                            val userStatus = resource.data!!
                            cachedUserStatus = userStatus
                            
                            // 보유 종목들을 WebSocket 구독 목록에 추가
                            val stockCodes = userStatus.holdings.map { it.stockCode }
                            smartWebSocketService.updatePortfolioStocks(stockCodes)
                            
                            // 실시간 데이터와 결합하여 포트폴리오 계산
                            updatePortfolioWithRealTimeData(userStatus)
                        }
                        is Resource.Error -> {
                            android.util.Log.e("BotPortfolioViewModel", "❌ 매매봇 API 에러: ${resource.message}")
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
                android.util.Log.e("BotPortfolioViewModel", "💥 매매봇 예외 발생: ${e.localizedMessage}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "매매봇 데이터 로드 중 오류가 발생했습니다: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * 실시간 데이터 업데이트 감시 (성능 최적화)
     */
    private fun observeRealTimeUpdates() {
        viewModelScope.launch {
            // 1초마다 한 번만 업데이트 (쓰로틀링)
            realTimeStockCache.quotes
                .sample(1000.milliseconds)
                .collect { quotesMap ->
                    cachedUserStatus?.let { userStatus ->
                        updatePortfolioWithRealTimeData(userStatus)
                    }
                }
        }
    }

    /**
     * 실시간 데이터와 결합하여 포트폴리오 계산
     */
    private fun updatePortfolioWithRealTimeData(userStatus: UserCurrentStatusDto) {
        viewModelScope.launch {
            try {
                // 실시간 가격 데이터 가져오기
                val realTimePrices = realTimeStockCache.quotes.value
                
                // 포트폴리오 계산
                val portfolioSummary = PortfolioCalculator.calculateMyPagePortfolio(
                    userStatus = userStatus,
                    realTimePrices = realTimePrices
                )
                
                // 비율 계산
                val holdingsWithWeights = PortfolioCalculator.calculateHoldingWeights(
                    portfolioSummary.holdings
                )
                
                // 파이차트 데이터 생성
                val pieChartData = PortfolioCalculator.createPieChartData(holdingsWithWeights)
                
                // UI 상태 업데이트
                _uiState.update { 
                    it.copy(
                        portfolioSummary = portfolioSummary.copy(holdings = holdingsWithWeights),
                        pieChartData = pieChartData,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "실시간 데이터 처리 중 오류가 발생했습니다: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * 포맷팅된 금액 반환
     */
    fun formatAmount(amount: Long): String {
        return when {
            amount >= 100_000_000 -> "${amount / 100_000_000}억${if (amount % 100_000_000 != 0L) " ${(amount % 100_000_000) / 10_000}만" else ""}원"
            amount >= 10_000 -> "${amount / 10_000}만${if (amount % 10_000 != 0L) " ${amount % 10_000}" else ""}원"
            else -> "${amount}원"
        }
    }

    /**
     * 수익률 색상 반환
     */
    fun getProfitLossColor(profitLoss: Long): androidx.compose.ui.graphics.Color {
        return when {
            profitLoss > 0 -> androidx.compose.ui.graphics.Color.Red
            profitLoss < 0 -> androidx.compose.ui.graphics.Color.Blue
            else -> androidx.compose.ui.graphics.Color.Gray
        }
    }

    /**
     * 수익률 텍스트 반환
     */
    fun formatProfitLoss(profitLoss: Long, profitRate: Double): String {
        val sign = if (profitLoss > 0) "+" else ""
        return "${sign}${formatAmount(profitLoss)} (${sign}${String.format("%.2f", profitRate)}%)"
    }
}
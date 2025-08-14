package com.lago.app.presentation.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.data.cache.RealTimeStockCache
import com.lago.app.data.local.prefs.UserPreferences
import com.lago.app.domain.repository.UserRepository
import com.lago.app.data.remote.dto.MyPagePortfolioSummary
import com.lago.app.data.remote.dto.UserCurrentStatusDto
import com.lago.app.data.remote.dto.HoldingResponseDto
import com.lago.app.data.remote.websocket.SmartStockWebSocketService
import com.lago.app.data.scheduler.SmartUpdateScheduler
import com.lago.app.data.service.CloseDataService
import com.lago.app.util.MarketTimeUtils
import com.lago.app.util.PortfolioCalculator
import com.lago.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import javax.inject.Inject

/**
 * 홈 화면용 주식 데이터
 */
data class HomeStock(
    val stockCode: String,
    val stockName: String,
    val quantity: Int,
    val totalPurchaseAmount: Long,
    val currentPrice: Double?,
    val profitLoss: Long,
    val profitRate: Double
)

/**
 * 홈 화면 UI 상태
 */
data class HomeUiState(
    val portfolioSummary: MyPagePortfolioSummary? = null,
    val stockList: List<HomeStock> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val refreshing: Boolean = false,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences,
    private val realTimeStockCache: RealTimeStockCache,
    private val smartWebSocketService: SmartStockWebSocketService,
    private val smartUpdateScheduler: SmartUpdateScheduler,
    private val closeDataService: CloseDataService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    // 캐시된 API 데이터
    private var cachedUserStatus: UserCurrentStatusDto? = null

    init {
        checkLoginStatus()
        
        // 시장 상태에 따라 다른 데이터 소스 사용
        if (MarketTimeUtils.isMarketOpen()) {
            android.util.Log.d("HomeViewModel", "🏢 장 시간 - 실시간 모드")
            observeRealTimeUpdates()
        } else {
            android.util.Log.d("HomeViewModel", "🔒 장 마감 - 종가 모드")
        }
        
        loadUserPortfolio()
    }

    /**
     * 로그인 상태 확인
     */
    private fun checkLoginStatus() {
        val isLoggedIn = userPreferences.getAuthToken() != null
        _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
        
        if (isLoggedIn) {
            loadUserPortfolio()
        }
    }

    /**
     * 사용자 포트폴리오 로드 (API 호출)
     */
    fun loadUserPortfolio() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val userId = 5 // 임시 테스트용
                android.util.Log.d("HomeViewModel", "📡 API 요청 시작: userId=$userId")

                userRepository.getUserCurrentStatus(userId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            android.util.Log.d("HomeViewModel", "⏳ API 로딩 중...")
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is Resource.Success -> {
                            android.util.Log.d("HomeViewModel", "✅ API 성공: ${resource.data}")
                            val userStatus = resource.data!!
                            cachedUserStatus = userStatus
                            
                            // 보유 종목들을 WebSocket 구독 목록에 추가
                            val stockCodes = userStatus.holdings.map { it.stockCode }
                            
                            if (MarketTimeUtils.isMarketOpen()) {
                                // 장 시간: WebSocket 실시간 데이터 사용
                                smartWebSocketService.updatePortfolioStocks(stockCodes)
                                updatePortfolioWithRealTimeData(userStatus)
                            } else {
                                // 장 마감: REST API 종가 데이터 사용
                                updatePortfolioWithCloseData(userStatus)
                            }
                        }
                        is Resource.Error -> {
                            android.util.Log.e("HomeViewModel", "❌ API 에러: ${resource.message}")
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
                android.util.Log.e("HomeViewModel", "💥 예외 발생: ${e.localizedMessage}", e)
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
                
                // 홈 화면용 주식 데이터 생성
                val stockList = userStatus.holdings.map { holding ->
                    createHomeStock(holding, realTimePrices[holding.stockCode]?.price)
                }
                
                // UI 상태 업데이트
                _uiState.update { 
                    it.copy(
                        portfolioSummary = portfolioSummary,
                        stockList = stockList,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "💥 실시간 데이터 처리 오류: ${e.localizedMessage}", e)
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
     * 장 마감 시 종가 데이터로 포트폴리오 계산
     */
    private fun updatePortfolioWithCloseData(userStatus: UserCurrentStatusDto) {
        viewModelScope.launch {
            try {
                android.util.Log.d("HomeViewModel", "🔒 장 마감 - 종가 데이터로 포트폴리오 계산 시작")
                
                // 보유 종목 코드 리스트
                val stockCodes = userStatus.holdings.map { it.stockCode }
                android.util.Log.d("HomeViewModel", "📋 종가 조회 대상: ${stockCodes.joinToString()}")
                
                // REST API로 종가 데이터 조회
                val closePrices = closeDataService.getPortfolioClosePrices(stockCodes)
                android.util.Log.d("HomeViewModel", "💰 종가 데이터: $closePrices")
                
                // 종가를 실시간 가격 형태로 변환
                val closeRealTimePrices = closePrices.mapValues { (stockCode, closePrice) ->
                    com.lago.app.domain.entity.StockRealTimeData(
                        stockCode = stockCode,
                        closePrice = closePrice.toLong(),
                        tradePrice = closePrice.toLong(),
                        currentPrice = closePrice.toLong(),
                        changePrice = 0L,
                        changeRate = 0.0
                    )
                }
                
                // 포트폴리오 계산 (종가 기준)
                val portfolioSummary = PortfolioCalculator.calculateMyPagePortfolio(
                    userStatus = userStatus,
                    realTimePrices = closeRealTimePrices
                )
                
                // 홈 화면용 주식 데이터 생성 (종가 기준)
                val stockList = userStatus.holdings.map { holding ->
                    val closePrice = closePrices[holding.stockCode]?.toDouble()
                    createHomeStock(holding, closePrice)
                }
                
                // UI 상태 업데이트
                _uiState.update { 
                    it.copy(
                        portfolioSummary = portfolioSummary,
                        stockList = stockList,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                
                android.util.Log.d("HomeViewModel", "✅ 장 마감 포트폴리오 계산 완료")
                
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "❌ 종가 데이터 처리 오류: ${e.localizedMessage}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "종가 데이터 처리 중 오류가 발생했습니다: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * 홈 화면용 주식 데이터 생성
     */
    private fun createHomeStock(holding: HoldingResponseDto, currentPrice: Double?): HomeStock {
        val realPrice = currentPrice ?: (holding.totalPurchaseAmount.toDouble() / holding.quantity)
        val currentValue = realPrice * holding.quantity
        val profitLoss = currentValue.toLong() - holding.totalPurchaseAmount
        val profitRate = if (holding.totalPurchaseAmount > 0) {
            (profitLoss.toDouble() / holding.totalPurchaseAmount) * 100
        } else 0.0

        android.util.Log.d("HomeViewModel", "🏠 ${holding.stockName}: ${holding.quantity}개, 총매수 ${holding.totalPurchaseAmount}원, 현재가 ${realPrice}원, 평가손익 ${profitLoss}원")

        return HomeStock(
            stockCode = holding.stockCode,
            stockName = holding.stockName,
            quantity = holding.quantity,
            totalPurchaseAmount = holding.totalPurchaseAmount,
            currentPrice = currentPrice,
            profitLoss = profitLoss,
            profitRate = profitRate
        )
    }

    /**
     * 새로고침
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(refreshing = true) }
            
            android.util.Log.d("HomeViewModel", "🔄 새로고침: ${MarketTimeUtils.getMarketStatusString()}")
            
            loadUserPortfolio()
            _uiState.update { it.copy(refreshing = false) }
        }
    }

    /**
     * 로그인 후 호출
     */
    fun onLoginSuccess() {
        checkLoginStatus()
    }

    /**
     * 에러 메시지 클리어
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 포맷팅된 금액 반환
     */
    fun formatAmount(amount: Long): String {
        return String.format("%,d원", amount)
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
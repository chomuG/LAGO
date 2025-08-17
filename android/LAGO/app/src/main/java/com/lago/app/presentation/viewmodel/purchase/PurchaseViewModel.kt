package com.lago.app.presentation.viewmodel.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.StockDisplayInfo
import com.lago.app.domain.entity.StockHolding
import com.lago.app.domain.entity.AccountBalance
import com.lago.app.domain.entity.MockTradeResult
import com.lago.app.domain.repository.MockTradeRepository
import com.lago.app.domain.repository.PortfolioRepository
import com.lago.app.data.local.prefs.UserPreferences
import com.lago.app.data.remote.dto.AccountCurrentStatusResponse
import com.lago.app.data.remote.dto.StockHoldingResponse
import com.lago.app.data.remote.api.ChartApiService
import com.lago.app.util.KoreanStockMarketUtils
import com.lago.app.util.ChartInterval
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
    val tradeResult: MockTradeResult? = null,
    val accountType: Int = 0 // 0=실시간모의투자, 1=역사챌린지, 2=자동매매봇
)

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val mockTradeRepository: MockTradeRepository,
    private val portfolioRepository: PortfolioRepository,
    private val userPreferences: UserPreferences,
    private val chartApiService: ChartApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState

    fun loadStockInfo(stockCode: String, isPurchaseType: Boolean = true, accountType: Int = 0) {
        android.util.Log.d("PurchaseViewModel", "💰 주식정보 로딩 시작: stockCode=$stockCode, isPurchaseType=$isPurchaseType, accountType=$accountType")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // 1. 주식 정보 조회 (MockTradeRepository 사용 - 더 안정적인 API)
                mockTradeRepository.getStockDisplayInfo(stockCode).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is Resource.Success -> {
                            val stockInfo = resource.data!!
                            android.util.Log.d("PurchaseViewModel", "💰 주식정보 조회 성공: ${stockInfo.name}(${stockInfo.code}) ${stockInfo.currentPrice}원")
                            
                            android.util.Log.d("PurchaseViewModel", "💰 주식정보를 UI에 임시 반영: ${stockInfo.name}, ${stockInfo.currentPrice}원")
                            
                            // 먼저 주식 기본 정보를 UI에 반영
                            _uiState.update { state ->
                                state.copy(
                                    stockCode = stockInfo.code,
                                    stockName = stockInfo.name,
                                    currentPrice = stockInfo.currentPrice,
                                    isPurchaseType = isPurchaseType,
                                    accountType = accountType,
                                    isLoading = true // 계좌 정보 로딩 중
                                )
                            }
                            
                            // 2. 계좌 잔고 조회 (별도 코루틴에서 실행)
                            launch {
                                loadAccountInfo(stockInfo, isPurchaseType, accountType)
                            }
                        }
                        is Resource.Error -> {
                            android.util.Log.e("PurchaseViewModel", "💰 주식정보 조회 실패: ${resource.message}")
                            
                            // 장 마감 시간이나 주말에는 마지막 일봉 데이터를 시도
                            launch {
                                tryGetLastStockData(stockCode, isPurchaseType, accountType)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PurchaseViewModel", "💰 주식정보 로드 중 예외 발생: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "주식 정보 로드 중 오류가 발생했습니다: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private suspend fun loadAccountInfo(stockInfo: StockDisplayInfo, isPurchaseType: Boolean, accountType: Int) {
        // PortfolioRepository를 사용하여 계좌 정보 조회
        val userId = userPreferences.getUserIdLong()
        if (userId == 0L) {
            android.util.Log.e("PurchaseViewModel", "💰 계좌정보 로딩 실패: userId가 0L (로그인 안됨)")
            return
        }
        android.util.Log.d("PurchaseViewModel", "💰 계좌정보 로딩 시작: userId=$userId, accountType=$accountType")
        
        portfolioRepository.getUserCurrentStatus(userId, accountType).collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    android.util.Log.d("PurchaseViewModel", "💰 계좌정보 로딩 중...")
                }
                is Resource.Success -> {
                    val accountStatus = resource.data!!
                    android.util.Log.d("PurchaseViewModel", "💰 계좌정보 조회 성공: 잔액=${accountStatus.balance}원, 보유종목=${accountStatus.holdings.size}개")
                    
                    if (isPurchaseType) {
                        // 매수: 계좌 잔고 기반으로 최대 구매 가능 금액 설정
                        android.util.Log.d("PurchaseViewModel", "💰 매수 모드: 잔액=${accountStatus.balance}원으로 설정")
                        _uiState.update { state ->
                            val updatedState = state.copy(
                                stockCode = stockInfo.code,
                                stockName = if (stockInfo.name.isNotBlank()) stockInfo.name else state.stockName, // 기존 값 보존
                                currentPrice = if (stockInfo.currentPrice > 0) stockInfo.currentPrice else state.currentPrice, // 기존 값 보존
                                holdingInfo = "${String.format("%,d", accountStatus.balance)}원",
                                isPurchaseType = isPurchaseType,
                                maxAmount = accountStatus.balance.toLong(),
                                accountBalance = accountStatus.balance.toLong(),
                                holdingQuantity = 0,
                                accountType = accountType,
                                isLoading = false
                            )
                            android.util.Log.d("PurchaseViewModel", "💰 UI 상태 업데이트 완료: stockName=${updatedState.stockName}, currentPrice=${updatedState.currentPrice}, accountBalance=${updatedState.accountBalance}")
                            updatedState
                        }
                    } else {
                        // 매도: 보유 주식 수량 조회
                        loadHoldingInfo(stockInfo, accountStatus, accountType)
                    }
                }
                is Resource.Error -> {
                    android.util.Log.e("PurchaseViewModel", "💰 계좌정보 조회 실패: ${resource.message}")
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

    private suspend fun loadHoldingInfo(stockInfo: StockDisplayInfo, accountStatus: AccountCurrentStatusResponse, accountType: Int) {
        // PortfolioRepository에서 보유 주식 정보 조회 (accountStatus.holdings 사용)
        val holding = accountStatus.holdings.find { it.stockCode == stockInfo.code }
        
        _uiState.update { state ->
            state.copy(
                stockCode = stockInfo.code,
                stockName = if (stockInfo.name.isNotBlank()) stockInfo.name else state.stockName, // 기존 값 보존
                currentPrice = if (stockInfo.currentPrice > 0) stockInfo.currentPrice else state.currentPrice, // 기존 값 보존
                holdingInfo = if (holding != null) {
                    "${holding.quantity}주 보유 (평균 ${String.format("%,d", holding.totalPurchaseAmount / holding.quantity)}원)"
                } else {
                    "보유 주식 없음"
                },
                isPurchaseType = false,
                maxAmount = (holding?.quantity ?: 0) * stockInfo.currentPrice.toLong(),
                accountBalance = accountStatus.balance.toLong(),
                holdingQuantity = holding?.quantity ?: 0,
                accountType = accountType,
                isLoading = false
            )
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
            price = state.currentPrice,
            accountType = state.accountType
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
            price = state.currentPrice,
            accountType = state.accountType
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

    /**
     * 장 마감 시간이나 실시간 데이터 실패 시 마지막 일봉 데이터로 주식 정보 조회
     */
    private suspend fun tryGetLastStockData(stockCode: String, isPurchaseType: Boolean, accountType: Int) {
        try {
            android.util.Log.d("PurchaseViewModel", "💰 마지막 일봉 데이터 조회 시도: $stockCode")
            
            // 먼저 현재 영업일 정보를 로그로 확인
            KoreanStockMarketUtils.logTradingDayInfo()
            
            // KoreanStockMarketUtils를 사용하여 영업일 기준 날짜 범위 생성 (DateTime 형식)
            val (startDateTime, endDateTime) = KoreanStockMarketUtils.getChartDateTimeRange()
            android.util.Log.d("PurchaseViewModel", "💰 DateTime 범위: $startDateTime ~ $endDateTime")
            android.util.Log.d("PurchaseViewModel", "💰 API 호출: api/stocks/$stockCode?interval=DAY&fromDateTime=$startDateTime&toDateTime=$endDateTime")
            
            val intervalData = chartApiService.getIntervalChartData(stockCode, ChartInterval.DAY.value, startDateTime, endDateTime)
            android.util.Log.d("PurchaseViewModel", "💰 인터벌 API 응답: ${intervalData.size}개 데이터 수신")
            
            if (intervalData.isNotEmpty()) {
                // 가장 최근 데이터 사용
                val latestData = intervalData.last()
                val stockName = getStockNameByCode(stockCode)
                
                android.util.Log.d("PurchaseViewModel", "💰 마지막 일봉 데이터 조회 성공: $stockName ${latestData.closePrice}원 (${latestData.bucket})")
                
                val stockInfo = StockDisplayInfo(
                    stockInfoId = latestData.stockInfoId,
                    code = latestData.code,
                    name = stockName,
                    market = "KOSPI",
                    currentPrice = latestData.closePrice.toInt(),
                    openPrice = latestData.openPrice.toInt(),
                    highPrice = latestData.highPrice.toInt(),
                    lowPrice = latestData.lowPrice.toInt(),
                    volume = latestData.volume,
                    priceChange = (latestData.closePrice - latestData.openPrice).toInt(),
                    priceChangeRate = ((latestData.closePrice - latestData.openPrice).toDouble() / latestData.openPrice * 100),
                    updatedAt = latestData.bucket.split("T")[0], // "2024-08-13T09:00:00" → "2024-08-13"
                    isFavorite = false
                )
                
                // UI에 반영
                _uiState.update { state ->
                    state.copy(
                        stockCode = stockInfo.code,
                        stockName = stockInfo.name,
                        currentPrice = stockInfo.currentPrice,
                        isPurchaseType = isPurchaseType,
                        accountType = accountType,
                        isLoading = true // 계좌 정보 로딩 중
                    )
                }
                
                // 계좌 정보 로드
                loadAccountInfo(stockInfo, isPurchaseType, accountType)
                
            } else {
                android.util.Log.e("PurchaseViewModel", "💰 일봉 데이터도 없음: $stockCode")
                // 정말 마지막 수단으로 기본 정보 사용
                val stockName = getStockNameByCode(stockCode)
                if (stockName.isNotEmpty()) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            stockCode = stockCode,
                            stockName = stockName,
                            currentPrice = 1000, // 최후의 기본값
                            errorMessage = "최신 주식 데이터를 가져올 수 없습니다. 기본 정보를 표시합니다."
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "주식 정보를 찾을 수 없습니다. 올바른 종목코드를 확인해주세요."
                        )
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PurchaseViewModel", "💰 일봉 데이터 조회 실패: ${e.message}", e)
            
            // 예외 타입별 상세 로그
            when (e) {
                is retrofit2.HttpException -> {
                    android.util.Log.e("PurchaseViewModel", "💰 HTTP 에러: ${e.code()} - ${e.message()}")
                    android.util.Log.e("PurchaseViewModel", "💰 에러 응답: ${e.response()?.errorBody()?.string()}")
                }
                is java.io.IOException -> {
                    android.util.Log.e("PurchaseViewModel", "💰 네트워크 에러: ${e.message}")
                }
                else -> {
                    android.util.Log.e("PurchaseViewModel", "💰 기타 에러: ${e.javaClass.simpleName} - ${e.message}")
                }
            }
            
            // 최후의 수단으로 기본 정보 사용
            val stockName = getStockNameByCode(stockCode)
            if (stockName.isNotEmpty()) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        stockCode = stockCode,
                        stockName = stockName,
                        currentPrice = 1000, // 최후의 기본값
                        errorMessage = "주식 데이터를 가져올 수 없습니다. 네트워크를 확인해주세요."
                    )
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "주식 정보를 찾을 수 없습니다."
                    )
                }
            }
        }
    }

    /**
     * 주식 코드로 주식명 조회 (API 기반)
     * ChartApiService의 getStockInfo API를 사용하여 종목명 조회
     */
    private suspend fun getStockNameByCode(stockCode: String): String {
        // 현재 로드된 주식 정보에서 이름 가져오기 (우선순위)
        val currentName = _uiState.value.stockName.takeIf { it.isNotEmpty() }
        if (currentName != null) return currentName
        
        try {
            android.util.Log.d("PurchaseViewModel", "💰 API로 종목명 조회 시도: $stockCode")
            val stockInfo = chartApiService.getStockInfo(stockCode)
            android.util.Log.d("PurchaseViewModel", "💰 API로 종목명 조회 성공: ${stockInfo.name}")
            return stockInfo.name
        } catch (e: Exception) {
            android.util.Log.e("PurchaseViewModel", "💰 API로 종목명 조회 실패: ${e.message}", e)
            // API 실패 시 종목코드 그대로 반환
            return stockCode
        }
    }
}
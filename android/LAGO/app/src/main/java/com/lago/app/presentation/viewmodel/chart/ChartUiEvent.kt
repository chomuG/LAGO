package com.lago.app.presentation.viewmodel.chart

import com.lago.app.domain.entity.SignalSource

sealed class ChartUiEvent {
    data class ChangeStock(val stockCode: String) : ChartUiEvent()
    data class ChangeStockWithInfo(val stockCode: String, val stockInfo: com.lago.app.domain.entity.ChartStockInfo) : ChartUiEvent()
    data class ChangeTimeFrame(val timeFrame: String) : ChartUiEvent()
    data class ToggleIndicator(val indicatorType: String, val enabled: Boolean) : ChartUiEvent()
    object RefreshData : ChartUiEvent()
    object ClearError : ChartUiEvent()
    object ToggleFavorite : ChartUiEvent()
    data class ChangeBottomTab(val tabIndex: Int) : ChartUiEvent()
    object AnalyzePattern : ChartUiEvent()
    object BackPressed : ChartUiEvent()
    object BuyClicked : ChartUiEvent()
    object SellClicked : ChartUiEvent()
    object ShowIndicatorSettings : ChartUiEvent()
    object HideIndicatorSettings : ChartUiEvent()
    object ToggleIndicatorSettings : ChartUiEvent()
    object LoadTradingSignals : ChartUiEvent()
    data class ToggleUserTradingSignals(val show: Boolean) : ChartUiEvent()
    data class SelectAITradingSignals(val aiSource: SignalSource?) : ChartUiEvent()
    object ClearTradeMessage : ChartUiEvent()
    // UpdatePanelSizes 이벤트 제거 - 단순화된 구조에서는 불필요
}
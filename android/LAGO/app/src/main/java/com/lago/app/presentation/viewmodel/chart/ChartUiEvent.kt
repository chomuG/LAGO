package com.lago.app.presentation.viewmodel.chart

sealed class ChartUiEvent {
    data class ChangeStock(val stockCode: String) : ChartUiEvent()
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
}
package com.lago.app.util

import com.lago.app.domain.entity.PortfolioReturn
import com.lago.app.domain.entity.StockRealTimeData
import com.lago.app.domain.entity.TotalPortfolioSummary
import com.lago.app.presentation.viewmodel.chart.HoldingItem

object PortfolioCalculator {
    
    /**
     * 단일 종목의 실시간 수익률 계산
     */
    fun calculateStockReturn(
        holding: HoldingItem,
        realTimePrice: Double
    ): PortfolioReturn {
        val quantity = parseQuantity(holding.quantity)
        val buyPrice = holding.value.toDouble() / quantity
        
        val profit = (realTimePrice - buyPrice) * quantity
        val returnRate = if (buyPrice > 0) ((realTimePrice - buyPrice) / buyPrice) * 100 else 0.0
        val totalValue = realTimePrice * quantity
        
        return PortfolioReturn(
            stockCode = holding.stockCode,
            stockName = holding.name,
            quantity = quantity,
            buyPrice = buyPrice,
            currentPrice = realTimePrice,
            profit = profit,
            returnRate = returnRate,
            totalValue = totalValue
        )
    }
    
    /**
     * 전체 포트폴리오의 실시간 수익률 계산
     */
    fun calculateTotalPortfolioReturn(
        holdings: List<HoldingItem>,
        realTimePrices: Map<String, StockRealTimeData>
    ): TotalPortfolioSummary {
        val stockReturns = holdings.mapNotNull { holding ->
            val realTimeData = realTimePrices[holding.stockCode]
            if (realTimeData != null) {
                calculateStockReturn(holding, realTimeData.currentPrice)
            } else null
        }
        
        val totalInvestment = stockReturns.sumOf { it.buyPrice * it.quantity }
        val totalCurrentValue = stockReturns.sumOf { it.totalValue }
        val totalProfit = totalCurrentValue - totalInvestment
        val totalReturnRate = if (totalInvestment > 0) (totalProfit / totalInvestment) * 100 else 0.0
        
        return TotalPortfolioSummary(
            totalInvestment = totalInvestment,
            totalCurrentValue = totalCurrentValue,
            totalProfit = totalProfit,
            totalReturnRate = totalReturnRate,
            stockReturns = stockReturns
        )
    }
    
    /**
     * 배치로 여러 종목의 수익률 한번에 계산
     */
    fun batchCalculateReturns(
        holdings: List<HoldingItem>,
        realTimePrices: Map<String, StockRealTimeData>
    ): Map<String, PortfolioReturn> {
        return holdings.associate { holding ->
            val realTimeData = realTimePrices[holding.stockCode]
            val portfolioReturn = if (realTimeData != null) {
                calculateStockReturn(holding, realTimeData.currentPrice)
            } else {
                // 실시간 데이터가 없으면 기존 가격으로 계산
                val quantity = parseQuantity(holding.quantity)
                val buyPrice = holding.value.toDouble() / quantity
                PortfolioReturn(
                    stockCode = holding.stockCode,
                    stockName = holding.name,
                    quantity = quantity,
                    buyPrice = buyPrice,
                    currentPrice = buyPrice,
                    profit = 0.0,
                    returnRate = 0.0,
                    totalValue = holding.value.toDouble()
                )
            }
            holding.stockCode to portfolioReturn
        }
    }
    
    /**
     * 증감률 기준으로 포트폴리오 정렬
     */
    fun sortByReturnRate(stockReturns: List<PortfolioReturn>, ascending: Boolean = false): List<PortfolioReturn> {
        return if (ascending) {
            stockReturns.sortedBy { it.returnRate }
        } else {
            stockReturns.sortedByDescending { it.returnRate }
        }
    }
    
    /**
     * 수익 금액 기준으로 포트폴리오 정렬
     */
    fun sortByProfit(stockReturns: List<PortfolioReturn>, ascending: Boolean = false): List<PortfolioReturn> {
        return if (ascending) {
            stockReturns.sortedBy { it.profit }
        } else {
            stockReturns.sortedByDescending { it.profit }
        }
    }
    
    /**
     * 종목별 비중 계산
     */
    fun calculateWeights(summary: TotalPortfolioSummary): Map<String, Double> {
        return if (summary.totalCurrentValue > 0) {
            summary.stockReturns.associate { stock ->
                stock.stockCode to (stock.totalValue / summary.totalCurrentValue * 100)
            }
        } else {
            emptyMap()
        }
    }
    
    /**
     * 위험도가 높은 종목 찾기 (변동성이 큰 종목)
     */
    fun findHighRiskStocks(
        stockReturns: List<PortfolioReturn>,
        riskThreshold: Double = 10.0
    ): List<PortfolioReturn> {
        return stockReturns.filter { kotlin.math.abs(it.returnRate) > riskThreshold }
    }
    
    /**
     * 실시간 포트폴리오 알림이 필요한 종목들
     */
    fun getAlertStocks(
        stockReturns: List<PortfolioReturn>,
        profitAlertThreshold: Double = 5.0,
        lossAlertThreshold: Double = -3.0
    ): List<Pair<PortfolioReturn, String>> {
        return stockReturns.mapNotNull { stock ->
            when {
                stock.returnRate >= profitAlertThreshold -> 
                    stock to "수익률 ${String.format("%.2f", stock.returnRate)}% 달성!"
                stock.returnRate <= lossAlertThreshold -> 
                    stock to "손실률 ${String.format("%.2f", kotlin.math.abs(stock.returnRate))}% 발생"
                else -> null
            }
        }
    }
    
    private fun parseQuantity(quantityString: String): Int {
        return quantityString.replace("주", "").replace(",", "").trim().toIntOrNull() ?: 0
    }
    
    /**
     * 포트폴리오 성과 통계 계산
     */
    fun calculatePerformanceStats(summary: TotalPortfolioSummary): Map<String, Any> {
        val profits = summary.stockReturns.map { it.profit }
        val returns = summary.stockReturns.map { it.returnRate }
        
        return mapOf(
            "winning_stocks" to summary.stockReturns.count { it.profit > 0 },
            "losing_stocks" to summary.stockReturns.count { it.profit < 0 },
            "max_profit" to (profits.maxOrNull() ?: 0.0),
            "max_loss" to (profits.minOrNull() ?: 0.0),
            "avg_return" to (returns.average().takeIf { !it.isNaN() } ?: 0.0),
            "return_volatility" to calculateVolatility(returns)
        )
    }
    
    private fun calculateVolatility(returns: List<Double>): Double {
        if (returns.isEmpty()) return 0.0
        val mean = returns.average()
        val variance = returns.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance)
    }
}
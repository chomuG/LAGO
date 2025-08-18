package com.lago.app.data.mapper

import com.lago.app.data.remote.dto.*
import com.lago.app.domain.entity.*

object ChartDataMapper {

    fun StockInfoDto.toDomain(): ChartStockInfo {
        return ChartStockInfo(
            code = code,
            name = name,
            currentPrice = currentPrice.toFloat(),
            priceChange = priceChange.toFloat(),
            priceChangePercent = priceChangeRate.toFloat(),
            previousDay = null
        )
    }

    fun CandlestickDto.toDomain(): CandlestickData {
        return CandlestickData(
            time = time,
            open = open,
            high = high,
            low = low,
            close = close,
            volume = volume
        )
    }

    fun VolumeDto.toDomain(): VolumeData {
        return VolumeData(
            time = time,
            value = value,
            color = color
        )
    }

    fun LineDataDto.toDomain(): LineData {
        return LineData(
            time = time,
            value = value
        )
    }

    fun MACDDto.toDomain(): MACDResult {
        return MACDResult(
            macdLine = macdLine.map { it.toDomain() },
            signalLine = signalLine.map { it.toDomain() },
            histogram = histogram.map { it.toDomain() }
        )
    }

    fun BollingerBandsDto.toDomain(): BollingerBandsResult {
        return BollingerBandsResult(
            upperBand = upperBand.map { it.toDomain() },
            middleBand = middleBand.map { it.toDomain() },
            lowerBand = lowerBand.map { it.toDomain() }
        )
    }

    fun HoldingDto.toDomain(): HoldingItem {
        return HoldingItem(
            stockCode = stockCode,
            stockName = stockName,
            quantity = quantity,
            averagePrice = averagePrice,
            currentPrice = currentPrice,
            profitLoss = profitLoss,
            profitLossPercent = profitLossPercent,
            totalValue = totalValue
        )
    }

    fun TradingDto.toDomain(): TradingItem {
        return TradingItem(
            transactionId = transactionId,
            stockCode = stockCode,
            stockName = stockName,
            actionType = actionType,
            quantity = quantity,
            price = price,
            totalAmount = totalAmount,
            createdAt = createdAt
        )
    }

    fun List<CandlestickDto>.toCandlestickDataList(): List<CandlestickData> {
        return map { it.toDomain() }
    }

    fun List<VolumeDto>.toVolumeDataList(): List<VolumeData> {
        return map { it.toDomain() }
    }

    fun List<LineDataDto>.toLineDataList(): List<LineData> {
        return map { it.toDomain() }
    }

    fun List<HoldingDto>.toHoldingItemList(): List<HoldingItem> {
        return map { it.toDomain() }
    }

    fun List<TradingDto>.toTradingItemList(): List<TradingItem> {
        return map { it.toDomain() }
    }

    // StockList mappers
    fun StockItemDto.toDomain(): StockItem {
        return StockItem(
            code = code,
            name = name,
            market = market,
            currentPrice = currentPrice,
            priceChange = priceChange,
            priceChangePercent = priceChangePercent,
            volume = volume,
            marketCap = marketCap,
            sector = sector,
            isFavorite = isFavorite,
            updatedAt = updatedAt
        )
    }

    fun StockListPageDto.toDomain(): StockListPage {
        return StockListPage(
            content = content.map { it.toStockItem() },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages
        )
    }

    fun List<StockItemDto>.toStockItemList(): List<StockItem> {
        return map { it.toDomain() }
    }

    // Simple Stock mappers (actual API format)
    fun SimpleStockDto.toDomain(): StockItem {
        return StockItem(
            code = code,
            name = name,
            market = market,
            currentPrice = 0, // API에서 제공하지 않음, 기본값
            priceChange = 0, // API에서 제공하지 않음, 기본값
            priceChangePercent = 0.0, // API에서 제공하지 않음, 기본값
            volume = 0L, // API에서 제공하지 않음, 기본값
            marketCap = null,
            sector = null,
            isFavorite = false, // 기본값
            updatedAt = "" // API에서 제공하지 않음, 기본값
        )
    }

    fun List<SimpleStockDto>.toStockItemListFromSimple(): List<StockItem> {
        return map { it.toDomain() }
    }

    fun List<SimpleStockDto>.toStockListPage(): StockListPage {
        return StockListPage(
            content = map { it.toDomain() },
            page = 0,
            size = size,
            totalElements = size.toLong(),
            totalPages = 1
        )
    }

    // StockInfoDto를 StockItem으로 변환
    fun StockInfoDto.toStockItem(): StockItem {
        return StockItem(
            code = code,
            name = name,
            market = market,
            currentPrice = currentPrice,
            priceChange = priceChange,
            priceChangePercent = priceChangeRate,
            volume = volume,
            marketCap = null, // StockInfoDto에 없는 필드
            sector = null,    // StockInfoDto에 없는 필드
            isFavorite = false, // 기본값
            updatedAt = updatedAt
        )
    }
}
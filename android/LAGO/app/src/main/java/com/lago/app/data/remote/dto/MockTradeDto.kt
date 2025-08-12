package com.lago.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 모의투자 매수/매도 요청 DTO
 */
data class MockTradeRequest(
    @SerializedName("stockCode")
    val stockCode: String,
    @SerializedName("quantity") 
    val quantity: Int,
    @SerializedName("price")
    val price: Int  // 주당 가격
)

/**
 * 모의투자 매수/매도 응답 DTO
 */
data class MockTradeResponse(
    @SerializedName("tradeId")
    val tradeId: Long,
    @SerializedName("stockCode")
    val stockCode: String,
    @SerializedName("stockName")
    val stockName: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("price")
    val price: Int,
    @SerializedName("totalAmount")
    val totalAmount: Long,
    @SerializedName("commission")
    val commission: Int,
    @SerializedName("tradeAt")
    val tradeAt: String,
    @SerializedName("remainingBalance")
    val remainingBalance: Long
)

/**
 * 거래내역 DTO (MOCK_TRADE 테이블 기반)
 */
data class MockTradeHistoryDto(
    @SerializedName("tradeId")
    val tradeId: Long,
    @SerializedName("stockCode")
    val stockCode: String,
    @SerializedName("stockName")
    val stockName: String,
    @SerializedName("buySell")
    val buySell: String,    // "BUY" or "SELL"
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("price")
    val price: Int,
    @SerializedName("totalAmount")
    val totalAmount: Long,
    @SerializedName("commission")
    val commission: Int,
    @SerializedName("tradeAt")
    val tradeAt: String
)

/**
 * 거래내역 페이징 응답 DTO
 */
data class MockTradeHistoryResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message") 
    val message: String? = null,
    @SerializedName("data")
    val data: MockTradeHistoryPageDto
)

data class MockTradeHistoryPageDto(
    @SerializedName("content")
    val content: List<MockTradeHistoryDto>,
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("totalElements")
    val totalElements: Long,
    @SerializedName("totalPages")
    val totalPages: Int
)
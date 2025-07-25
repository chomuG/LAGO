package com.lago.app.domain.entity

import java.time.LocalDateTime

data class User(
    val id: String,
    val email: String,
    val name: String,
    val profileImageUrl: String? = null,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null,
    val investmentLevel: InvestmentLevel = InvestmentLevel.BEGINNER,
    val preferredRiskLevel: RiskLevel = RiskLevel.LOW,
    val totalInvestment: Double = 0.0,
    val totalReturn: Double = 0.0,
    val isVerified: Boolean = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    val returnRate: Double
        get() = if (totalInvestment > 0) (totalReturn / totalInvestment) * 100 else 0.0
    
    val isActive: Boolean
        get() = isVerified && totalInvestment > 0
}

enum class InvestmentLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT
}

enum class RiskLevel {
    VERY_LOW,
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH
}

data class UserPortfolio(
    val userId: String,
    val totalValue: Double,
    val cashBalance: Double,
    val investments: List<Investment>,
    val lastUpdated: LocalDateTime
)

data class Investment(
    val id: String,
    val symbol: String,
    val name: String,
    val quantity: Int,
    val averageCost: Double,
    val currentPrice: Double,
    val totalValue: Double,
    val unrealizedGainLoss: Double,
    val unrealizedGainLossPercent: Double
)
package com.lago.app.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "users")
data class User(
    @PrimaryKey
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
    val createdAt: Date,
    val updatedAt: Date
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
    val lastUpdated: Date
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
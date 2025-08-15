package com.lago.app.domain.repository

import com.lago.app.domain.entity.Transaction

interface TransactionRepository {
    suspend fun getTransactions(userId: Long): Result<List<Transaction>>
    suspend fun getHistoryTransactions(userId: Long): Result<List<Transaction>>
}
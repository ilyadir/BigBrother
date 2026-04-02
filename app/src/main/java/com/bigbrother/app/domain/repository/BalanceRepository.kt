package com.bigbrother.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface BalanceRepository {
    fun getAvailableMinutes(): Flow<Int>
    suspend fun addEarn(minutes: Int, note: String, category: String)
    suspend fun addSpend(minutes: Int, note: String)
}

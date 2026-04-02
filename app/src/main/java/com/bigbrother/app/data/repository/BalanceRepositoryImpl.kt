package com.bigbrother.app.data.repository

import com.bigbrother.app.data.local.dao.BalanceDao
import com.bigbrother.app.data.local.entity.BalanceEntryEntity
import com.bigbrother.app.domain.model.EntryType
import com.bigbrother.app.domain.repository.BalanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceRepositoryImpl @Inject constructor(
    private val balanceDao: BalanceDao
) : BalanceRepository {

    override fun getAvailableMinutes(): Flow<Int> = balanceDao.streamBalance()

    override suspend fun addEarn(minutes: Int, note: String, category: String) {
        balanceDao.insert(
            BalanceEntryEntity(
                createdAt = System.currentTimeMillis(),
                minutes = minutes,
                type = EntryType.EARN,
                note = note,
                category = category
            )
        )
    }

    override suspend fun addSpend(minutes: Int, note: String) {
        balanceDao.insert(
            BalanceEntryEntity(
                createdAt = System.currentTimeMillis(),
                minutes = minutes,
                type = EntryType.SPEND,
                note = note,
                category = null
            )
        )
    }
}

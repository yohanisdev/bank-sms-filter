package com.example.hermodit.domain.usecase

import com.example.hermodit.domain.model.DailyTransactionGroup
import com.example.hermodit.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe transactions grouped by calendar day (newest to oldest).
 */
class GetDailyTransactionGroupsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<DailyTransactionGroup>> {
        return repository.getDailyTransactionGroups()
    }
}

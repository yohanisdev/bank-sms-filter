package com.example.hermodit.domain.usecase

import com.example.hermodit.domain.model.FinancialSummary
import com.example.hermodit.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe the current financial summary (balance, income, expenses).
 */
class GetFinancialSummaryUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<FinancialSummary> {
        return repository.getFinancialSummary()
    }
}

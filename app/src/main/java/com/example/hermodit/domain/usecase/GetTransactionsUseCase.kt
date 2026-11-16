package com.example.hermodit.domain.usecase

import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.model.TransactionType
import com.example.hermodit.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case to retrieve financial transactions, optionally filtered by transaction type.
 */
class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(typeFilter: TransactionType? = null): Flow<List<FinancialTransaction>> {
        return repository.getTransactions().map { transactions ->
            if (typeFilter == null) {
                transactions
            } else {
                transactions.filter { it.type == typeFilter }
            }
        }
    }
}

package com.example.hermodit.domain.usecase

import com.example.hermodit.core.result.AppResult
import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.repository.TransactionRepository
import java.math.BigDecimal

/**
 * Use case to validate and record a financial transaction.
 */
class AddTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: FinancialTransaction): AppResult<Unit> {
        if (transaction.amount <= BigDecimal.ZERO) {
            return AppResult.Error("Transaction amount must be greater than zero")
        }
        if (transaction.bankName.isBlank()) {
            return AppResult.Error("Bank name cannot be blank")
        }
        repository.addTransaction(transaction)
        return AppResult.Success(Unit)
    }
}

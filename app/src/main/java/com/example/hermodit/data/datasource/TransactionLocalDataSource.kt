package com.example.hermodit.data.datasource

import com.example.hermodit.domain.model.FinancialTransaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Contract for local transaction storage.
 */
interface TransactionLocalDataSource {
    fun getTransactions(): Flow<List<FinancialTransaction>>
    fun getTransactionsForDate(date: LocalDate): Flow<List<FinancialTransaction>>
    suspend fun getTransactionById(id: String): FinancialTransaction?
    suspend fun insertTransaction(transaction: FinancialTransaction)
    suspend fun insertTransactions(transactions: List<FinancialTransaction>)
    suspend fun deleteTransaction(id: String)
}

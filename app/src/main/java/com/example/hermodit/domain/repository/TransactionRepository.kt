package com.example.hermodit.domain.repository

import com.example.hermodit.domain.model.DailyTransactionGroup
import com.example.hermodit.domain.model.FinancialSummary
import com.example.hermodit.domain.model.FinancialTransaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface defining operations for managing financial transactions and daily groups.
 */
interface TransactionRepository {
    fun getTransactions(): Flow<List<FinancialTransaction>>
    fun getDailyTransactionGroups(): Flow<List<DailyTransactionGroup>>
    fun getTransactionsForDate(date: LocalDate): Flow<List<FinancialTransaction>>
    suspend fun getTransactionById(id: String): FinancialTransaction?
    suspend fun addTransaction(transaction: FinancialTransaction)
    suspend fun addTransactions(transactions: List<FinancialTransaction>)
    suspend fun deleteTransaction(id: String)
    fun getFinancialSummary(): Flow<FinancialSummary>
}

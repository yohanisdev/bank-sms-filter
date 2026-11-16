package com.example.hermodit.domain.repository

import com.example.hermodit.domain.model.BankAccountConfig
import com.example.hermodit.domain.model.DailyTransactionGroup
import com.example.hermodit.domain.model.FinancialSummary
import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.model.UnmatchedSmsRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface defining operations for managing financial transactions,
 * daily groups, monitored account configuration, and unmatched SMS audit records.
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
    suspend fun exists(sourceId: String, bankReferenceNumber: String?): Boolean
    suspend fun getTransactionCount(): Int
    fun getMonitoredAccountConfig(): Flow<BankAccountConfig>
    suspend fun saveMonitoredAccountConfig(config: BankAccountConfig)
    suspend fun recordUnmatchedSms(record: UnmatchedSmsRecord)
    fun getUnmatchedSmsRecords(): Flow<List<UnmatchedSmsRecord>>
    suspend fun deleteUnmatchedSmsRecord(id: String)
    suspend fun getUnmatchedSmsCount(): Int
}

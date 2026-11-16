package com.example.hermodit.data.repository

import com.example.hermodit.data.datasource.TransactionLocalDataSource
import com.example.hermodit.data.local.dao.BankAccountConfigDao
import com.example.hermodit.data.local.dao.UnmatchedSmsDao
import com.example.hermodit.data.local.entity.BankAccountConfigEntity
import com.example.hermodit.data.local.entity.UnmatchedSmsEntity
import com.example.hermodit.domain.model.BankAccountConfig
import com.example.hermodit.domain.model.DailySummary
import com.example.hermodit.domain.model.DailyTransactionGroup
import com.example.hermodit.domain.model.FinancialSummary
import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.model.TransactionType
import com.example.hermodit.domain.model.UnmatchedSmsRecord
import com.example.hermodit.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

/**
 * Implementation of [TransactionRepository] supporting daily grouping,
 * accurate bank ledger aggregation, Room configuration persistence, and unmatched SMS logging.
 */
class TransactionRepositoryImpl(
    private val localDataSource: TransactionLocalDataSource,
    private val configDao: BankAccountConfigDao? = null,
    private val unmatchedSmsDao: UnmatchedSmsDao? = null
) : TransactionRepository {

    private val inMemoryConfigFlow = MutableStateFlow(BankAccountConfig())
    private val inMemoryUnmatchedFlow = MutableStateFlow<List<UnmatchedSmsRecord>>(emptyList())

    override fun getTransactions(): Flow<List<FinancialTransaction>> {
        return localDataSource.getTransactions()
    }

    override fun getDailyTransactionGroups(): Flow<List<DailyTransactionGroup>> {
        return localDataSource.getTransactions().map { transactions ->
            transactions.groupBy { it.transactionDate }
                .entries
                .sortedByDescending { it.key }
                .map { (date, txs) ->
                    var received = BigDecimal.ZERO
                    var deposited = BigDecimal.ZERO
                    var withdrawn = BigDecimal.ZERO

                    for (tx in txs) {
                        when (tx.type) {
                            TransactionType.RECEIVED -> received = received.add(tx.amount)
                            TransactionType.DEPOSIT -> deposited = deposited.add(tx.amount)
                            TransactionType.WITHDRAWAL -> withdrawn = withdrawn.add(tx.amount)
                        }
                    }

                    val netMovement = received.add(deposited).subtract(withdrawn)

                    // Rule: Use latest reported balance on that date (never synthesized by calculation)
                    val latestReportedBalance = txs
                        .sortedWith(
                            compareByDescending<FinancialTransaction> { it.transactionTime ?: LocalTime.MIN }
                                .thenByDescending { it.smsTimestamp }
                        )
                        .firstOrNull { it.availableBalance != null }
                        ?.availableBalance

                    DailyTransactionGroup(
                        summary = DailySummary(
                            date = date,
                            totalReceived = received,
                            totalDeposited = deposited,
                            totalWithdrawn = withdrawn,
                            netMovement = netMovement,
                            latestReportedBalance = latestReportedBalance,
                            transactionCount = txs.size,
                            currency = txs.firstOrNull()?.currency ?: "ETB"
                        ),
                        transactions = txs.sortedWith(
                            compareByDescending<FinancialTransaction> { it.transactionTime ?: LocalTime.MIN }
                                .thenByDescending { it.smsTimestamp }
                        )
                    )
                }
        }
    }

    override fun getTransactionsForDate(date: LocalDate): Flow<List<FinancialTransaction>> {
        return localDataSource.getTransactionsForDate(date).map { list ->
            list.sortedWith(
                compareByDescending<FinancialTransaction> { it.transactionTime ?: LocalTime.MIN }
                    .thenByDescending { it.smsTimestamp }
            )
        }
    }

    override suspend fun getTransactionById(id: String): FinancialTransaction? {
        return localDataSource.getTransactionById(id)
    }

    override suspend fun addTransaction(transaction: FinancialTransaction) {
        localDataSource.insertTransaction(transaction)
    }

    override suspend fun addTransactions(transactions: List<FinancialTransaction>) {
        localDataSource.insertTransactions(transactions)
    }

    override suspend fun deleteTransaction(id: String) {
        localDataSource.deleteTransaction(id)
    }

    override suspend fun exists(sourceId: String, bankReferenceNumber: String?): Boolean {
        return localDataSource.exists(sourceId, bankReferenceNumber)
    }

    override suspend fun getTransactionCount(): Int {
        return localDataSource.getTransactionCount()
    }

    override fun getMonitoredAccountConfig(): Flow<BankAccountConfig> {
        return if (configDao != null) {
            configDao.getConfigFlow().map { entity ->
                entity?.toDomain() ?: BankAccountConfig()
            }
        } else {
            inMemoryConfigFlow.asStateFlow()
        }
    }

    override suspend fun saveMonitoredAccountConfig(config: BankAccountConfig) {
        if (configDao != null) {
            configDao.saveConfig(BankAccountConfigEntity.fromDomain(config))
        } else {
            inMemoryConfigFlow.value = config
        }
    }

    override suspend fun recordUnmatchedSms(record: UnmatchedSmsRecord) {
        if (unmatchedSmsDao != null) {
            unmatchedSmsDao.insert(UnmatchedSmsEntity.fromDomain(record))
        } else {
            inMemoryUnmatchedFlow.value = listOf(record) + inMemoryUnmatchedFlow.value
        }
    }

    override fun getUnmatchedSmsRecords(): Flow<List<UnmatchedSmsRecord>> {
        return if (unmatchedSmsDao != null) {
            unmatchedSmsDao.getAll().map { list -> list.map { it.toDomain() } }
        } else {
            inMemoryUnmatchedFlow.asStateFlow()
        }
    }

    override suspend fun deleteUnmatchedSmsRecord(id: String) {
        if (unmatchedSmsDao != null) {
            unmatchedSmsDao.deleteById(id)
        } else {
            inMemoryUnmatchedFlow.value = inMemoryUnmatchedFlow.value.filterNot { it.id == id }
        }
    }

    override suspend fun getUnmatchedSmsCount(): Int {
        return if (unmatchedSmsDao != null) {
            unmatchedSmsDao.count()
        } else {
            inMemoryUnmatchedFlow.value.size
        }
    }

    override fun getFinancialSummary(): Flow<FinancialSummary> {
        return localDataSource.getTransactions().map { transactions ->
            var income = BigDecimal.ZERO
            var expense = BigDecimal.ZERO

            for (tx in transactions) {
                when (tx.type) {
                    TransactionType.RECEIVED, TransactionType.DEPOSIT -> income = income.add(tx.amount)
                    TransactionType.WITHDRAWAL -> expense = expense.add(tx.amount)
                }
            }

            // Find the most recent reported balance from all transactions
            val latestBalance = transactions
                .sortedWith(
                    compareByDescending<FinancialTransaction> { it.transactionDate }
                        .thenByDescending { it.transactionTime ?: LocalTime.MIN }
                        .thenByDescending { it.smsTimestamp }
                )
                .firstOrNull { it.availableBalance != null }
                ?.availableBalance ?: BigDecimal.ZERO

            FinancialSummary(
                totalBalance = latestBalance,
                totalIncome = income,
                totalExpenses = expense,
                transactionCount = transactions.size,
                currency = "ETB"
            )
        }
    }
}

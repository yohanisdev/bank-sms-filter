package com.example.hermodit.data.repository

import com.example.hermodit.data.datasource.TransactionLocalDataSource
import com.example.hermodit.domain.model.DailySummary
import com.example.hermodit.domain.model.DailyTransactionGroup
import com.example.hermodit.domain.model.FinancialSummary
import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.model.TransactionType
import com.example.hermodit.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

/**
 * Default implementation of [TransactionRepository] supporting daily grouping and accurate bank ledger aggregation.
 */
class TransactionRepositoryImpl(
    private val localDataSource: TransactionLocalDataSource
) : TransactionRepository {

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

package com.example.hermodit.data.datasource

import com.example.hermodit.data.local.dao.TransactionDao
import com.example.hermodit.data.local.entity.TransactionEntity
import com.example.hermodit.domain.model.FinancialTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Room-backed implementation of [TransactionLocalDataSource].
 */
class RoomTransactionDataSource(
    private val transactionDao: TransactionDao
) : TransactionLocalDataSource {

    override fun getTransactions(): Flow<List<FinancialTransaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsForDate(date: LocalDate): Flow<List<FinancialTransaction>> {
        return transactionDao.getTransactionsForDate(date).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTransactionById(id: String): FinancialTransaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun insertTransaction(transaction: FinancialTransaction) {
        transactionDao.insert(TransactionEntity.fromDomain(transaction))
    }

    override suspend fun insertTransactions(transactions: List<FinancialTransaction>) {
        transactionDao.insertAll(transactions.map { TransactionEntity.fromDomain(it) })
    }

    override suspend fun deleteTransaction(id: String) {
        transactionDao.deleteById(id)
    }

    override suspend fun exists(sourceId: String, bankReferenceNumber: String?): Boolean {
        return transactionDao.exists(sourceId, bankReferenceNumber)
    }

    override suspend fun getTransactionCount(): Int {
        return transactionDao.count()
    }
}

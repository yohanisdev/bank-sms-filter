package com.example.hermodit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hermodit.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, transactionTime DESC, smsTimestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE transactionDate = :date ORDER BY transactionTime DESC, smsTimestamp DESC")
    fun getTransactionsForDate(date: LocalDate): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM transactions 
            WHERE sourceIdentifier = :sourceId 
               OR (bankReferenceNumber IS NOT NULL AND bankReferenceNumber != '' AND bankReferenceNumber = :bankReferenceNumber)
        )
    """)
    suspend fun exists(sourceId: String, bankReferenceNumber: String?): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM transactions")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int
}

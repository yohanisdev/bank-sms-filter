package com.example.hermodit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hermodit.data.local.entity.UnmatchedSmsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO providing persistent local review queue storage for unmatched or ambiguous SMS messages.
 */
@Dao
interface UnmatchedSmsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(unmatched: UnmatchedSmsEntity): Long

    @Query("SELECT * FROM unmatched_sms ORDER BY receivedTimestamp DESC")
    fun getAll(): Flow<List<UnmatchedSmsEntity>>

    @Query("SELECT * FROM unmatched_sms WHERE id = :id")
    suspend fun getById(id: String): UnmatchedSmsEntity?

    @Query("DELETE FROM unmatched_sms WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM unmatched_sms")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM unmatched_sms")
    suspend fun count(): Int
}

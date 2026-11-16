package com.example.hermodit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hermodit.data.local.entity.BankAccountConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankAccountConfigDao {
    @Query("SELECT * FROM bank_account_config WHERE id = 1")
    fun getConfigFlow(): Flow<BankAccountConfigEntity?>

    @Query("SELECT * FROM bank_account_config WHERE id = 1")
    suspend fun getConfig(): BankAccountConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: BankAccountConfigEntity): Long
}

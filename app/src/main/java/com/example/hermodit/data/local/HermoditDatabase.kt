package com.example.hermodit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hermodit.data.local.dao.BankAccountConfigDao
import com.example.hermodit.data.local.dao.TransactionDao
import com.example.hermodit.data.local.dao.UnmatchedSmsDao
import com.example.hermodit.data.local.entity.BankAccountConfigEntity
import com.example.hermodit.data.local.entity.TransactionEntity
import com.example.hermodit.data.local.entity.UnmatchedSmsEntity

@Database(
    entities = [
        TransactionEntity::class,
        BankAccountConfigEntity::class,
        UnmatchedSmsEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HermoditDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun bankAccountConfigDao(): BankAccountConfigDao
    abstract fun unmatchedSmsDao(): UnmatchedSmsDao

    companion object {
        @Volatile
        private var INSTANCE: HermoditDatabase? = null

        fun getInstance(context: Context): HermoditDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HermoditDatabase::class.java,
                    "hermodit_local.db"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

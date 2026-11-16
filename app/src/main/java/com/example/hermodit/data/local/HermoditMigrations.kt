package com.example.hermodit.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Non-destructive migration from Version 1 to Version 2.
 *
 * Changes in Version 2:
 * Added 6 new columns to the `transactions` table:
 * 1. rawSenderName TEXT DEFAULT NULL
 * 2. serviceCharge TEXT DEFAULT NULL
 * 3. vat TEXT DEFAULT NULL
 * 4. disasterRecoveryCharge TEXT DEFAULT NULL
 * 5. totalDebit TEXT DEFAULT NULL
 * 6. parseStatus TEXT NOT NULL DEFAULT 'CONFIRMED'
 *
 * All existing transaction records, configuration, and unmatched SMS records
 * are strictly preserved without data loss.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `rawSenderName` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `serviceCharge` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `vat` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `disasterRecoveryCharge` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `totalDebit` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `parseStatus` TEXT NOT NULL DEFAULT 'CONFIRMED'")
    }
}

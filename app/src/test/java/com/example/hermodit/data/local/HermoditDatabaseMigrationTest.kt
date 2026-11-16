package com.example.hermodit.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * Robust migration tests verifying:
 * 1. Safe upgrade from Version 1 schema to Version 2 schema.
 * 2. Complete data preservation of existing transactions, bank config, and unmatched SMS.
 * 3. Correct default values for newly added fields (parseStatus = 'CONFIRMED', fees = NULL).
 * 4. Deduplication constraints preservation (UNIQUE index on sourceIdentifier).
 * 5. Full ability to write and read Version 2 records post-migration.
 */
class HermoditDatabaseMigrationTest {

    private lateinit var connection: Connection

    @Before
    fun setUp() {
        // In-memory real SQLite database instance
        connection = DriverManager.getConnection("jdbc:sqlite::memory:")
        createVersion1Schema(connection)
    }

    @After
    fun tearDown() {
        if (!connection.isClosed) {
            connection.close()
        }
    }

    @Test
    fun migration_1_to_2_preservesExistingDataAndAddsNewFields() {
        // 1. Insert Version 1 production fixtures
        insertVersion1Transaction(
            id = "tx-prev-001",
            bankName = "Commercial Bank of Ethiopia",
            accountIdentifier = "•••• 3608",
            type = "RECEIVED",
            amount = "10000.00",
            currency = "ETB",
            counterpartyName = "Fintech Corp",
            counterpartyAccount = "1000123456789",
            transactionDate = "2026-11-15",
            transactionTime = "14:30:00",
            availableBalance = "25000.00",
            bankReferenceNumber = "FT1234567890",
            rawSmsContent = "Raw SMS text from CBE",
            sourceIdentifier = "sms-inbox-101",
            smsTimestamp = 1731671400000L,
            smsSenderId = "CBE",
            channel = "TRANSFER"
        )

        insertVersion1BankConfig(
            id = 1,
            bankName = "Commercial Bank of Ethiopia",
            bankCode = "CBE",
            accountIdentifier = "•••• 3608",
            fullAccountNumber = "1000763003608",
            accountHolder = "YOANNIS KEFYALEW AND MEKLIT ASHENAF",
            accountType = "SAVINGS",
            authorizedSenderIds = "CBE,CBE_Birr,889,telebirr",
            isSmsShieldActive = 1,
            lastSyncTimestamp = 1731671400000L
        )

        insertVersion1UnmatchedSms(
            id = "unmatched-prev-001",
            senderAddress = "CBE",
            messageBody = "Ambiguous message for YOANNIS",
            receivedTimestamp = 1731671400000L,
            reason = "Mentions holder name without account number"
        )

        // 2. Execute explicit Room Migration 1 -> 2
        val supportDb = createSupportSQLiteDatabaseProxy(connection)
        MIGRATION_1_2.migrate(supportDb)

        // 3. Verify Version 1 transaction was strictly preserved without data loss
        connection.prepareStatement("SELECT * FROM transactions WHERE id = 'tx-prev-001'").use { stmt ->
            val rs = stmt.executeQuery()
            assertTrue("Existing transaction must be preserved after migration", rs.next())
            assertEquals("Commercial Bank of Ethiopia", rs.getString("bankName"))
            assertEquals("•••• 3608", rs.getString("accountIdentifier"))
            assertEquals("RECEIVED", rs.getString("type"))
            assertEquals("10000.00", rs.getString("amount"))
            assertEquals("ETB", rs.getString("currency"))
            assertEquals("Fintech Corp", rs.getString("counterpartyName"))
            assertEquals("1000123456789", rs.getString("counterpartyAccount"))
            assertEquals("2026-11-15", rs.getString("transactionDate"))
            assertEquals("14:30:00", rs.getString("transactionTime"))
            assertEquals("25000.00", rs.getString("availableBalance"))
            assertEquals("FT1234567890", rs.getString("bankReferenceNumber"))
            assertEquals("sms-inbox-101", rs.getString("sourceIdentifier"))
            assertEquals(1731671400000L, rs.getLong("smsTimestamp"))
            assertEquals("CBE", rs.getString("smsSenderId"))
            assertEquals("TRANSFER", rs.getString("channel"))

            // Verify newly added fields migrated with correct defaults
            assertNull("rawSenderName should default to NULL for v1 records", rs.getString("rawSenderName"))
            assertNull("serviceCharge should default to NULL for v1 records", rs.getString("serviceCharge"))
            assertNull("vat should default to NULL for v1 records", rs.getString("vat"))
            assertNull("disasterRecoveryCharge should default to NULL for v1 records", rs.getString("disasterRecoveryCharge"))
            assertNull("totalDebit should default to NULL for v1 records", rs.getString("totalDebit"))
            assertEquals("parseStatus must default to 'CONFIRMED'", "CONFIRMED", rs.getString("parseStatus"))
        }

        // 4. Verify bank configuration was preserved
        connection.prepareStatement("SELECT * FROM bank_account_config WHERE id = 1").use { stmt ->
            val rs = stmt.executeQuery()
            assertTrue("Bank account config must be preserved", rs.next())
            assertEquals("1000763003608", rs.getString("fullAccountNumber"))
            assertEquals("YOANNIS KEFYALEW AND MEKLIT ASHENAF", rs.getString("accountHolder"))
        }

        // 5. Verify unmatched review queue was preserved
        connection.prepareStatement("SELECT * FROM unmatched_sms WHERE id = 'unmatched-prev-001'").use { stmt ->
            val rs = stmt.executeQuery()
            assertTrue("Unmatched review queue records must be preserved", rs.next())
            assertEquals("Ambiguous message for YOANNIS", rs.getString("messageBody"))
        }

        // 6. Verify ability to insert and read Version 2 records with new fields
        connection.prepareStatement("""
            INSERT INTO transactions (
                id, bankName, accountIdentifier, type, amount, currency,
                counterpartyName, counterpartyAccount, rawSenderName,
                transactionDate, transactionTime, availableBalance, bankReferenceNumber,
                rawSmsContent, sourceIdentifier, smsTimestamp, smsSenderId, channel,
                serviceCharge, vat, disasterRecoveryCharge, totalDebit, parseStatus
            ) VALUES (
                'tx-v2-002', 'Commercial Bank of Ethiopia', '•••• 3608', 'WITHDRAWAL', '310.00', 'ETB',
                'Gutema Mitiku Gemeda', '...1626', 'Gutema Mitiku Gemeda',
                '2026-11-16', '10:00:00', '4200.00', 'FT987654',
                'Outgoing transfer SMS', 'sms-inbox-102', 1731744000000, 'CBE', 'TRANSFER',
                '0.50', '0.08', '0.03', '310.61', 'CONFIRMED'
            )
        """).use { stmt ->
            stmt.executeUpdate()
        }

        connection.prepareStatement("SELECT * FROM transactions WHERE id = 'tx-v2-002'").use { stmt ->
            val rs = stmt.executeQuery()
            assertTrue(rs.next())
            assertEquals("310.00", rs.getString("amount"))
            assertEquals("0.50", rs.getString("serviceCharge"))
            assertEquals("0.08", rs.getString("vat"))
            assertEquals("0.03", rs.getString("disasterRecoveryCharge"))
            assertEquals("310.61", rs.getString("totalDebit"))
            assertEquals("CONFIRMED", rs.getString("parseStatus"))
        }

        // 7. Verify deduplication constraint (UNIQUE index on sourceIdentifier) survived migration
        try {
            connection.prepareStatement("""
                INSERT INTO transactions (
                    id, bankName, accountIdentifier, type, amount, currency,
                    transactionDate, sourceIdentifier, smsTimestamp, smsSenderId
                ) VALUES (
                    'tx-duplicate-test', 'CBE', '•••• 3608', 'RECEIVED', '50.00', 'ETB',
                    '2026-11-16', 'sms-inbox-101', 1731744000000, 'CBE'
                )
            """).use { stmt ->
                stmt.executeUpdate()
            }
            fail("Inserting a duplicate sourceIdentifier must fail due to UNIQUE index")
        } catch (e: SQLException) {
            assertTrue("Expected UNIQUE constraint violation but got: ${e.message}", e.message?.contains("UNIQUE") == true)
        }
    }

    @Test
    fun migration_withMultipleHistoricalTransactions_preservesAllRowsAndBalances() {
        for (i in 1..25) {
            insertVersion1Transaction(
                id = "tx-bulk-$i",
                bankName = "Commercial Bank of Ethiopia",
                accountIdentifier = "•••• 3608",
                type = if (i % 2 == 0) "WITHDRAWAL" else "RECEIVED",
                amount = "${i * 100}.00",
                currency = "ETB",
                counterpartyName = "Party $i",
                counterpartyAccount = "1000$i",
                transactionDate = "2026-11-${10 + (i % 5)}",
                transactionTime = "12:00:00",
                availableBalance = "${50000 - i * 100}.00",
                bankReferenceNumber = "FT$i",
                rawSmsContent = "SMS $i",
                sourceIdentifier = "sms-bulk-$i",
                smsTimestamp = 1731671400000L + i,
                smsSenderId = "CBE",
                channel = "TRANSFER"
            )
        }

        // Run migration
        val supportDb = createSupportSQLiteDatabaseProxy(connection)
        MIGRATION_1_2.migrate(supportDb)

        // Verify all 25 transactions survived with exact amounts and balances
        connection.prepareStatement("SELECT COUNT(*) AS total FROM transactions").use { stmt ->
            val rs = stmt.executeQuery()
            assertTrue(rs.next())
            assertEquals(25, rs.getInt("total"))
        }

        connection.prepareStatement("SELECT * FROM transactions WHERE id = 'tx-bulk-15'").use { stmt ->
            val rs = stmt.executeQuery()
            assertTrue(rs.next())
            assertEquals("1500.00", rs.getString("amount"))
            assertEquals("48500.00", rs.getString("availableBalance"))
            assertEquals("CONFIRMED", rs.getString("parseStatus"))
            assertNull(rs.getString("serviceCharge"))
        }
    }

    @Test
    fun migration_withNullOptionalFieldsInV1_handlesGracefully() {
        insertVersion1Transaction(
            id = "tx-nulls-001",
            bankName = "Commercial Bank of Ethiopia",
            accountIdentifier = "•••• 3608",
            type = "RECEIVED",
            amount = "10.00",
            currency = "ETB",
            counterpartyName = null,
            counterpartyAccount = null,
            transactionDate = "2026-11-16",
            transactionTime = null,
            availableBalance = null,
            bankReferenceNumber = null,
            rawSmsContent = null,
            sourceIdentifier = "sms-nulls-001",
            smsTimestamp = 1731744000000L,
            smsSenderId = "telebirr",
            channel = null
        )

        // Run migration
        val supportDb = createSupportSQLiteDatabaseProxy(connection)
        MIGRATION_1_2.migrate(supportDb)

        connection.prepareStatement("SELECT * FROM transactions WHERE id = 'tx-nulls-001'").use { stmt ->
            val rs = stmt.executeQuery()
            assertTrue(rs.next())
            assertEquals("10.00", rs.getString("amount"))
            assertNull(rs.getString("counterpartyName"))
            assertNull(rs.getString("availableBalance"))
            assertNull(rs.getString("bankReferenceNumber"))
            assertNull(rs.getString("rawSenderName"))
            assertNull(rs.getString("serviceCharge"))
            assertEquals("CONFIRMED", rs.getString("parseStatus"))
        }
    }

    private fun createVersion1Schema(conn: Connection) {
        conn.createStatement().use { stmt ->
            // Version 1 transactions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS `transactions` (
                    `id` TEXT NOT NULL,
                    `bankName` TEXT NOT NULL,
                    `accountIdentifier` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `amount` TEXT NOT NULL,
                    `currency` TEXT NOT NULL,
                    `counterpartyName` TEXT,
                    `counterpartyAccount` TEXT,
                    `transactionDate` TEXT NOT NULL,
                    `transactionTime` TEXT,
                    `availableBalance` TEXT,
                    `bankReferenceNumber` TEXT,
                    `rawSmsContent` TEXT,
                    `sourceIdentifier` TEXT NOT NULL,
                    `smsTimestamp` INTEGER NOT NULL,
                    `smsSenderId` TEXT NOT NULL,
                    `channel` TEXT,
                    PRIMARY KEY(`id`)
                )
            """)

            // Version 1 indices
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_transactions_sourceIdentifier` ON `transactions` (`sourceIdentifier`)")
            stmt.execute("CREATE INDEX IF NOT EXISTS `index_transactions_bankReferenceNumber` ON `transactions` (`bankReferenceNumber`)")
            stmt.execute("CREATE INDEX IF NOT EXISTS `index_transactions_transactionDate` ON `transactions` (`transactionDate`)")

            // Version 1 bank_account_config table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS `bank_account_config` (
                    `id` INTEGER NOT NULL,
                    `bankName` TEXT NOT NULL,
                    `bankCode` TEXT NOT NULL,
                    `accountIdentifier` TEXT NOT NULL,
                    `fullAccountNumber` TEXT NOT NULL,
                    `accountHolder` TEXT NOT NULL,
                    `accountType` TEXT NOT NULL,
                    `authorizedSenderIds` TEXT NOT NULL,
                    `isSmsShieldActive` INTEGER NOT NULL,
                    `lastSyncTimestamp` INTEGER,
                    PRIMARY KEY(`id`)
                )
            """)

            // Version 1 unmatched_sms table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS `unmatched_sms` (
                    `id` TEXT NOT NULL,
                    `senderAddress` TEXT NOT NULL,
                    `messageBody` TEXT NOT NULL,
                    `receivedTimestamp` INTEGER NOT NULL,
                    `reason` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
            """)
        }
    }

    private fun insertVersion1Transaction(
        id: String,
        bankName: String,
        accountIdentifier: String,
        type: String,
        amount: String,
        currency: String,
        counterpartyName: String?,
        counterpartyAccount: String?,
        transactionDate: String,
        transactionTime: String?,
        availableBalance: String?,
        bankReferenceNumber: String?,
        rawSmsContent: String?,
        sourceIdentifier: String,
        smsTimestamp: Long,
        smsSenderId: String,
        channel: String?
    ) {
        val sql = """
            INSERT INTO transactions (
                id, bankName, accountIdentifier, type, amount, currency,
                counterpartyName, counterpartyAccount, transactionDate, transactionTime,
                availableBalance, bankReferenceNumber, rawSmsContent, sourceIdentifier,
                smsTimestamp, smsSenderId, channel
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, id)
            stmt.setString(2, bankName)
            stmt.setString(3, accountIdentifier)
            stmt.setString(4, type)
            stmt.setString(5, amount)
            stmt.setString(6, currency)
            stmt.setString(7, counterpartyName)
            stmt.setString(8, counterpartyAccount)
            stmt.setString(9, transactionDate)
            stmt.setString(10, transactionTime)
            stmt.setString(11, availableBalance)
            stmt.setString(12, bankReferenceNumber)
            stmt.setString(13, rawSmsContent)
            stmt.setString(14, sourceIdentifier)
            stmt.setLong(15, smsTimestamp)
            stmt.setString(16, smsSenderId)
            stmt.setString(17, channel)
            stmt.executeUpdate()
        }
    }

    private fun insertVersion1BankConfig(
        id: Int,
        bankName: String,
        bankCode: String,
        accountIdentifier: String,
        fullAccountNumber: String,
        accountHolder: String,
        accountType: String,
        authorizedSenderIds: String,
        isSmsShieldActive: Int,
        lastSyncTimestamp: Long?
    ) {
        val sql = """
            INSERT INTO bank_account_config (
                id, bankName, bankCode, accountIdentifier, fullAccountNumber,
                accountHolder, accountType, authorizedSenderIds, isSmsShieldActive, lastSyncTimestamp
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            stmt.setString(2, bankName)
            stmt.setString(3, bankCode)
            stmt.setString(4, accountIdentifier)
            stmt.setString(5, fullAccountNumber)
            stmt.setString(6, accountHolder)
            stmt.setString(7, accountType)
            stmt.setString(8, authorizedSenderIds)
            stmt.setInt(9, isSmsShieldActive)
            if (lastSyncTimestamp != null) stmt.setLong(10, lastSyncTimestamp) else stmt.setNull(10, java.sql.Types.INTEGER)
            stmt.executeUpdate()
        }
    }

    private fun insertVersion1UnmatchedSms(
        id: String,
        senderAddress: String,
        messageBody: String,
        receivedTimestamp: Long,
        reason: String
    ) {
        val sql = "INSERT INTO unmatched_sms (id, senderAddress, messageBody, receivedTimestamp, reason) VALUES (?, ?, ?, ?, ?)"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, id)
            stmt.setString(2, senderAddress)
            stmt.setString(3, messageBody)
            stmt.setLong(4, receivedTimestamp)
            stmt.setString(5, reason)
            stmt.executeUpdate()
        }
    }

    private fun createSupportSQLiteDatabaseProxy(conn: Connection): SupportSQLiteDatabase {
        return Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            when (method.name) {
                "execSQL" -> {
                    val sql = args[0] as String
                    conn.createStatement().use { it.execute(sql) }
                    null
                }
                else -> throw UnsupportedOperationException("Method ${method.name} not implemented in test proxy")
            }
        } as SupportSQLiteDatabase
    }
}

package com.example.hermodit.data.repository

import com.example.hermodit.data.datasource.InMemoryTransactionDataSource
import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class TransactionRepositoryImplTest {

    private lateinit var repository: TransactionRepositoryImpl

    @Before
    fun setUp() {
        val dataSource = InMemoryTransactionDataSource()
        repository = TransactionRepositoryImpl(dataSource)
    }

    @Test
    fun getTransactions_returnsInitialSampleData() = runBlocking {
        val transactions = repository.getTransactions().first()
        assertTrue(transactions.isNotEmpty())
    }

    @Test
    fun addAndDeleteTransaction_persistsCorrectly() = runBlocking {
        val newTx = FinancialTransaction(
            id = "test-add-delete",
            bankName = "Commercial Bank of Ethiopia",
            accountIdentifier = "•••• 4582",
            type = TransactionType.RECEIVED,
            amount = BigDecimal("99.99"),
            counterpartyName = "Deposit Cash",
            transactionDate = LocalDate.now(),
            transactionTime = LocalTime.of(10, 0),
            sourceIdentifier = "sms-test-add",
            smsTimestamp = Instant.now()
        )

        repository.addTransaction(newTx)
        val fetched = repository.getTransactionById("test-add-delete")
        assertNotNull(fetched)
        assertEquals("Commercial Bank of Ethiopia", fetched?.bankName)

        repository.deleteTransaction("test-add-delete")
        val deleted = repository.getTransactionById("test-add-delete")
        assertNull(deleted)
    }

    @Test
    fun getDailyTransactionGroups_returnsOrderedGroupsWithAccurateCalculations() = runBlocking {
        val groups = repository.getDailyTransactionGroups().first()
        assertTrue("Daily groups should not be empty", groups.isNotEmpty())

        // Check newest to oldest ordering
        for (i in 0 until groups.size - 1) {
            assertTrue(
                "Groups must be ordered newest to oldest",
                groups[i].summary.date >= groups[i + 1].summary.date
            )
        }

        // Check calculation integrity for first group (Nov 16, 2026)
        val todayGroup = groups.first()
        val summary = todayGroup.summary
        assertEquals(
            summary.totalReceived.add(summary.totalDeposited).subtract(summary.totalWithdrawn),
            summary.netMovement
        )
        assertNotNull(summary.latestReportedBalance)
    }
}

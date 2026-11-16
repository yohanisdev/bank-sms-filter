package com.example.hermodit.domain.usecase

import com.example.hermodit.core.result.AppResult
import com.example.hermodit.data.datasource.InMemoryTransactionDataSource
import com.example.hermodit.data.repository.TransactionRepositoryImpl
import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class AddTransactionUseCaseTest {

    private lateinit var repository: TransactionRepositoryImpl
    private lateinit var useCase: AddTransactionUseCase

    @Before
    fun setUp() {
        val dataSource = InMemoryTransactionDataSource()
        repository = TransactionRepositoryImpl(dataSource)
        useCase = AddTransactionUseCase(repository)
    }

    @Test
    fun invoke_zeroOrNegativeAmount_returnsError() = runBlocking {
        val invalidTx = FinancialTransaction(
            id = "test-zero",
            bankName = "Commercial Bank of Ethiopia",
            accountIdentifier = "•••• 4582",
            type = TransactionType.WITHDRAWAL,
            amount = BigDecimal.ZERO,
            transactionDate = LocalDate.now(),
            sourceIdentifier = "sms-test-1",
            smsTimestamp = Instant.now()
        )

        val result = useCase(invalidTx)
        assertTrue(result is AppResult.Error)
        assertEquals("Transaction amount must be greater than zero", (result as AppResult.Error).message)
    }

    @Test
    fun invoke_blankBankName_returnsError() = runBlocking {
        val invalidTx = FinancialTransaction(
            id = "test-blank",
            bankName = "   ",
            accountIdentifier = "•••• 4582",
            type = TransactionType.WITHDRAWAL,
            amount = BigDecimal("10.00"),
            transactionDate = LocalDate.now(),
            sourceIdentifier = "sms-test-2",
            smsTimestamp = Instant.now()
        )

        val result = useCase(invalidTx)
        assertTrue(result is AppResult.Error)
        assertEquals("Bank name cannot be blank", (result as AppResult.Error).message)
    }

    @Test
    fun invoke_validTransaction_savesSuccessfully() = runBlocking {
        val validTx = FinancialTransaction(
            id = "test-valid-1",
            bankName = "Commercial Bank of Ethiopia",
            accountIdentifier = "•••• 4582",
            type = TransactionType.RECEIVED,
            amount = BigDecimal("49.99"),
            counterpartyName = "Abebe B.",
            transactionDate = LocalDate.now(),
            transactionTime = LocalTime.of(14, 30),
            sourceIdentifier = "sms-test-3",
            smsTimestamp = Instant.now()
        )

        val result = useCase(validTx)
        assertTrue(result is AppResult.Success)

        val stored = repository.getTransactions().first()
        assertTrue(stored.any { it.id == "test-valid-1" })
    }
}

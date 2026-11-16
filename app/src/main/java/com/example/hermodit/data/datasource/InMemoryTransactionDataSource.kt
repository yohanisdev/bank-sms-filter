package com.example.hermodit.data.datasource

import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Thread-safe in-memory transaction data source with initial sample records
 * matching the Stitch design specifications for verified offline rendering.
 */
class InMemoryTransactionDataSource : TransactionLocalDataSource {
    private val mutex = Mutex()
    private val transactionsFlow = MutableStateFlow<List<FinancialTransaction>>(createSampleTransactions())

    override fun getTransactions(): Flow<List<FinancialTransaction>> = transactionsFlow.asStateFlow()

    override fun getTransactionsForDate(date: LocalDate): Flow<List<FinancialTransaction>> {
        return transactionsFlow.map { list ->
            list.filter { it.transactionDate == date }
        }
    }

    override suspend fun getTransactionById(id: String): FinancialTransaction? {
        return mutex.withLock {
            transactionsFlow.value.find { it.id == id }
        }
    }

    override suspend fun insertTransaction(transaction: FinancialTransaction) {
        mutex.withLock {
            val current = transactionsFlow.value.toMutableList()
            current.removeAll { it.id == transaction.id }
            current.add(0, transaction)
            transactionsFlow.value = current
        }
    }

    override suspend fun insertTransactions(transactions: List<FinancialTransaction>) {
        mutex.withLock {
            val current = transactionsFlow.value.toMutableList()
            for (tx in transactions) {
                current.removeAll { it.id == tx.id }
                current.add(0, tx)
            }
            transactionsFlow.value = current
        }
    }

    override suspend fun deleteTransaction(id: String) {
        mutex.withLock {
            val current = transactionsFlow.value.toMutableList()
            current.removeAll { it.id == id }
            transactionsFlow.value = current
        }
    }

    override suspend fun exists(sourceId: String, bankReferenceNumber: String?): Boolean {
        return mutex.withLock {
            transactionsFlow.value.any { tx ->
                tx.sourceIdentifier == sourceId ||
                    (!bankReferenceNumber.isNullOrBlank() && tx.bankReferenceNumber == bankReferenceNumber)
            }
        }
    }

    override suspend fun getTransactionCount(): Int {
        return mutex.withLock {
            transactionsFlow.value.size
        }
    }

    companion object {
        fun createSampleTransactions(): List<FinancialTransaction> {
            val today = LocalDate.of(2026, 11, 16)
            val yesterday = today.minusDays(1)
            val dayBeforeYesterday = today.minusDays(2)
            val now = Instant.now()

            return listOf(
                // --- TODAY (Nov 16, 2026) --- Total: 10 txs | Received: 6,000 | Deposit: 2,500 (Total In: 8,500) | Withdrawn: 3,500 | Net: +5,000 | EOD: 25,000 ETB
                FinancialTransaction(
                    id = "tx-101",
                    bankName = "Commercial Bank of Ethiopia",
                    accountIdentifier = "•••• 4582",
                    type = TransactionType.RECEIVED,
                    amount = BigDecimal("6000.00"),
                    currency = "ETB",
                    counterpartyName = "Fintech Corp Ethiopia Ltd",
                    counterpartyAccount = "100099882231",
                    transactionDate = today,
                    transactionTime = LocalTime.of(10, 40),
                    availableBalance = BigDecimal("25000.00"),
                    bankReferenceNumber = "FT263219800451X",
                    rawSmsContent = "Dear Customer, ETB 6,000.00 transferred to your CBE Account ...4582 from Fintech Corp. Balance ETB 25,000.00. Ref: FT263219800451X.",
                    sourceIdentifier = "sms-cbe-101",
                    smsTimestamp = now
                ),
                FinancialTransaction(
                    id = "tx-102",
                    bankName = "Commercial Bank of Ethiopia",
                    accountIdentifier = "•••• 4582",
                    type = TransactionType.DEPOSIT,
                    amount = BigDecimal("2500.00"),
                    currency = "ETB",
                    counterpartyName = "Branch Cash Deposit",
                    counterpartyAccount = null,
                    transactionDate = today,
                    transactionTime = LocalTime.of(9, 15),
                    availableBalance = BigDecimal("19000.00"),
                    bankReferenceNumber = "FT263210088921B",
                    rawSmsContent = "Dear Customer, ETB 2,500.00 deposited to your CBE Account ...4582. Balance ETB 19,000.00. Ref: FT263210088921B.",
                    sourceIdentifier = "sms-cbe-102",
                    smsTimestamp = now.minusSeconds(3600)
                ),
                FinancialTransaction(
                    id = "tx-103",
                    bankName = "Commercial Bank of Ethiopia",
                    accountIdentifier = "•••• 4582",
                    type = TransactionType.WITHDRAWAL,
                    amount = BigDecimal("2000.00"),
                    currency = "ETB",
                    counterpartyName = "ATM Cash Withdrawal - Bole",
                    counterpartyAccount = null,
                    transactionDate = today,
                    transactionTime = LocalTime.of(12, 30),
                    availableBalance = BigDecimal("23000.00"),
                    bankReferenceNumber = "FT263211122334W",
                    rawSmsContent = "Dear Customer, ETB 2,000.00 debited from your CBE Account ...4582 at ATM. Balance ETB 23,000.00. Ref: FT263211122334W.",
                    sourceIdentifier = "sms-cbe-103",
                    smsTimestamp = now.minusSeconds(7200)
                ),
                FinancialTransaction(
                    id = "tx-104",
                    bankName = "Commercial Bank of Ethiopia",
                    accountIdentifier = "•••• 4582",
                    type = TransactionType.WITHDRAWAL,
                    amount = BigDecimal("1500.00"),
                    currency = "ETB",
                    counterpartyName = "Supermarket POS Payment",
                    counterpartyAccount = "100033445566",
                    transactionDate = today,
                    transactionTime = LocalTime.of(14, 45),
                    availableBalance = BigDecimal("21500.00"),
                    bankReferenceNumber = "FT263219988776P",
                    rawSmsContent = "Dear Customer, ETB 1,500.00 paid from CBE Account ...4582 to Fresh Corner. Balance ETB 21,500.00. Ref: FT263219988776P.",
                    sourceIdentifier = "sms-cbe-104",
                    smsTimestamp = now.minusSeconds(10800)
                ),

                // --- YESTERDAY (Nov 15, 2026) --- Total: 7 txs | Received: 4,000 | Withdrawn: 1,000 | Net: +3,000 | EOD: 20,000 ETB
                FinancialTransaction(
                    id = "tx-201",
                    bankName = "Commercial Bank of Ethiopia",
                    accountIdentifier = "•••• 4582",
                    type = TransactionType.RECEIVED,
                    amount = BigDecimal("4000.00"),
                    currency = "ETB",
                    counterpartyName = "Consulting Fee - Almaz T.",
                    counterpartyAccount = "100088776655",
                    transactionDate = yesterday,
                    transactionTime = LocalTime.of(15, 20),
                    availableBalance = BigDecimal("20000.00"),
                    bankReferenceNumber = "FT253199881122C",
                    rawSmsContent = "Dear Customer, ETB 4,000.00 transferred to your CBE Account ...4582 from Almaz T. Balance ETB 20,000.00. Ref: FT253199881122C.",
                    sourceIdentifier = "sms-cbe-201",
                    smsTimestamp = now.minusSeconds(86400)
                ),
                FinancialTransaction(
                    id = "tx-202",
                    bankName = "Commercial Bank of Ethiopia",
                    accountIdentifier = "•••• 4582",
                    type = TransactionType.WITHDRAWAL,
                    amount = BigDecimal("1000.00"),
                    currency = "ETB",
                    counterpartyName = "Utility Bill - Ethio Telecom",
                    counterpartyAccount = null,
                    transactionDate = yesterday,
                    transactionTime = LocalTime.of(11, 10),
                    availableBalance = BigDecimal("16000.00"),
                    bankReferenceNumber = "FT253144556677U",
                    rawSmsContent = "Dear Customer, ETB 1,000.00 debited from your CBE Account ...4582 for Ethio Telecom. Balance ETB 16,000.00. Ref: FT253144556677U.",
                    sourceIdentifier = "sms-cbe-202",
                    smsTimestamp = now.minusSeconds(90000)
                ),

                // --- DAY BEFORE YESTERDAY (Nov 14, 2026) --- Total: 4 txs | Received: 0 | Withdrawn: 2,150 | Net: -2,150 | EOD: 17,000 ETB
                FinancialTransaction(
                    id = "tx-301",
                    bankName = "Commercial Bank of Ethiopia",
                    accountIdentifier = "•••• 4582",
                    type = TransactionType.WITHDRAWAL,
                    amount = BigDecimal("2150.00"),
                    currency = "ETB",
                    counterpartyName = "Fuel Station Payment",
                    counterpartyAccount = "100044556677",
                    transactionDate = dayBeforeYesterday,
                    transactionTime = LocalTime.of(17, 45),
                    availableBalance = BigDecimal("17000.00"),
                    bankReferenceNumber = "FT243088771199F",
                    rawSmsContent = "Dear Customer, ETB 2,150.00 paid from your CBE Account ...4582 to Total Gas. Balance ETB 17,000.00. Ref: FT243088771199F.",
                    sourceIdentifier = "sms-cbe-301",
                    smsTimestamp = now.minusSeconds(172800)
                )
            )
        }
    }
}

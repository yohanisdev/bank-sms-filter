package com.example.hermodit.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Domain entity representing a financial transaction extracted or recorded in Hermodit.
 */
data class FinancialTransaction(
    val id: String,
    val bankName: String,
    val accountIdentifier: String,
    val type: TransactionType,
    val amount: BigDecimal,
    val currency: String = "ETB",
    val counterpartyName: String? = null,
    val counterpartyAccount: String? = null,
    val transactionDate: LocalDate,
    val transactionTime: LocalTime? = null,
    val availableBalance: BigDecimal? = null,
    val bankReferenceNumber: String? = null,
    val rawSmsContent: String? = null,
    val sourceIdentifier: String,
    val smsTimestamp: Instant
)

package com.example.hermodit.domain.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Aggregated summary for a single day of transactions.
 */
data class DailySummary(
    val date: LocalDate,
    val totalReceived: BigDecimal,
    val totalDeposited: BigDecimal,
    val totalWithdrawn: BigDecimal,
    val netMovement: BigDecimal,
    val latestReportedBalance: BigDecimal?,
    val transactionCount: Int,
    val currency: String = "ETB"
)

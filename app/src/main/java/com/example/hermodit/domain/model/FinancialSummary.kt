package com.example.hermodit.domain.model

import java.math.BigDecimal

/**
 * Aggregated financial summary over a period of time.
 */
data class FinancialSummary(
    val totalBalance: BigDecimal,
    val totalIncome: BigDecimal,
    val totalExpenses: BigDecimal,
    val transactionCount: Int,
    val currency: String = "USD"
)

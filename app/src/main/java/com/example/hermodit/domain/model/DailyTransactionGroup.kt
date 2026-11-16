package com.example.hermodit.domain.model

/**
 * Container holding a day's summary and its associated transactions.
 */
data class DailyTransactionGroup(
    val summary: DailySummary,
    val transactions: List<FinancialTransaction>
)

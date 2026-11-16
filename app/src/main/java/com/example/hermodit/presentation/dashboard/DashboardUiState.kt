package com.example.hermodit.presentation.dashboard

import com.example.hermodit.domain.model.BankAccountConfig
import com.example.hermodit.domain.model.DailyTransactionGroup
import com.example.hermodit.domain.model.FinancialSummary
import java.math.BigDecimal

/**
 * UI State for the Main Dashboard screen.
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val accountConfig: BankAccountConfig = BankAccountConfig(),
    val availableBalance: BigDecimal? = null,
    val dailyGroups: List<DailyTransactionGroup> = emptyList(),
    val summary: FinancialSummary = FinancialSummary(
        totalBalance = BigDecimal.ZERO,
        totalIncome = BigDecimal.ZERO,
        totalExpenses = BigDecimal.ZERO,
        transactionCount = 0,
        currency = "ETB"
    ),
    val userMessage: String? = null
)

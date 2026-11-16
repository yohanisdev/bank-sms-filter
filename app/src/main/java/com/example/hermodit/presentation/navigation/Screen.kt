package com.example.hermodit.presentation.navigation

import java.time.LocalDate

/**
 * Navigation destinations across Hermodit.
 */
sealed interface Screen {
    data object Dashboard : Screen
    data class DailyDetail(val date: LocalDate) : Screen
    data class TransactionDetail(val transactionId: String) : Screen
    data object AccountConfig : Screen
    data object Settings : Screen
}

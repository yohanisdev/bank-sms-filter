package com.example.hermodit.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.hermodit.domain.model.BankAccountConfig
import com.example.hermodit.presentation.config.AccountConfigScreen
import com.example.hermodit.presentation.dailydetail.DailyDetailScreen
import com.example.hermodit.presentation.dashboard.DashboardScreen
import com.example.hermodit.presentation.dashboard.DashboardViewModel
import com.example.hermodit.presentation.settings.SettingsScreen
import com.example.hermodit.presentation.txdetail.TransactionDetailScreen

/**
 * Navigation Host connecting all 5 designed Hermodit screens.
 */
@Composable
fun HermoditNavHost(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
    navigator: HermoditNavigator = rememberHermoditNavigator()
) {
    when (val screen = navigator.currentScreen) {
        is Screen.Dashboard -> {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToDailyDetail = { date -> navigator.navigateToDailyDetail(date) },
                onNavigateToAccountConfig = { navigator.navigateToAccountConfig() },
                onNavigateToSettings = { navigator.navigateToSettings() },
                modifier = modifier
            )
        }
        is Screen.DailyDetail -> {
            DailyDetailScreen(
                date = screen.date,
                repository = viewModel.repository,
                onNavigateBack = { navigator.popBack() },
                onNavigateToTransactionDetail = { txId -> navigator.navigateToTransactionDetail(txId) },
                modifier = modifier
            )
        }
        is Screen.TransactionDetail -> {
            TransactionDetailScreen(
                transactionId = screen.transactionId,
                repository = viewModel.repository,
                onNavigateBack = { navigator.popBack() },
                modifier = modifier
            )
        }
        is Screen.AccountConfig -> {
            AccountConfigScreen(
                initialConfig = BankAccountConfig(),
                onNavigateBack = { navigator.popBack() },
                modifier = modifier
            )
        }
        is Screen.Settings -> {
            SettingsScreen(
                onNavigateBack = { navigator.popBack() },
                onNavigateToDashboard = { navigator.navigateToDashboard() },
                onNavigateToTransactions = {
                    val latestDate = viewModel.uiState.value.dailyGroups.firstOrNull()?.summary?.date
                    if (latestDate != null) {
                        navigator.navigateToDailyDetail(latestDate)
                    } else {
                        navigator.navigateToDashboard()
                    }
                },
                modifier = modifier
            )
        }
    }
}

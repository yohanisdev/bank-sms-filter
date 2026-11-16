package com.example.hermodit.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

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
                onNavigateToDashboard = { navigator.navigateToDashboard() },
                onNavigateToSettings = { navigator.navigateToSettings() },
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
                initialConfig = uiState.accountConfig,
                onNavigateBack = { navigator.popBack() },
                onSaveConfig = { config -> viewModel.saveAccountConfig(config) },
                onStartScan = { viewModel.startInboxScan(context) },
                isScanning = uiState.scanProgress.isScanning,
                modifier = modifier
            )
        }
        is Screen.Settings -> {
            SettingsScreen(
                onNavigateBack = { navigator.popBack() },
                onNavigateToDashboard = { navigator.navigateToDashboard() },
                onNavigateToTransactions = {
                    val latestDate = uiState.dailyGroups.firstOrNull()?.summary?.date
                    if (latestDate != null) {
                        navigator.navigateToDailyDetail(latestDate)
                    } else {
                        navigator.navigateToDashboard()
                    }
                },
                onSyncNowClick = { viewModel.startInboxScan(context) },
                modifier = modifier
            )
        }
    }
}

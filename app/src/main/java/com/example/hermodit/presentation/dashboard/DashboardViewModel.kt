package com.example.hermodit.presentation.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.hermodit.data.datasource.RoomTransactionDataSource
import com.example.hermodit.data.local.HermoditDatabase
import com.example.hermodit.data.repository.TransactionRepositoryImpl
import com.example.hermodit.data.sms.HistoricalSmsScanner
import com.example.hermodit.data.sms.ScanProgress
import com.example.hermodit.domain.model.BankAccountConfig
import com.example.hermodit.domain.repository.TransactionRepository
import com.example.hermodit.domain.usecase.GetDailyTransactionGroupsUseCase
import com.example.hermodit.domain.usecase.GetFinancialSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing state for the Main Dashboard screen, powered by Room local storage
 * and the authorized historical SMS scanner.
 */
class DashboardViewModel(
    private val getDailyGroupsUseCase: GetDailyTransactionGroupsUseCase,
    private val getFinancialSummaryUseCase: GetFinancialSummaryUseCase,
    val repository: TransactionRepository,
    private val scanner: HistoricalSmsScanner = HistoricalSmsScanner(repository)
) : ViewModel() {

    private val scanProgressFlow = MutableStateFlow(ScanProgress())

    val uiState: StateFlow<DashboardUiState> = combine(
        getDailyGroupsUseCase(),
        getFinancialSummaryUseCase(),
        repository.getMonitoredAccountConfig(),
        scanProgressFlow
    ) { dailyGroups, summary, accountConfig, scanProgress ->
        val latestReportedBalance = dailyGroups
            .firstOrNull { it.summary.latestReportedBalance != null }
            ?.summary
            ?.latestReportedBalance

        DashboardUiState(
            isLoading = false,
            isSyncing = scanProgress.isScanning,
            accountConfig = accountConfig,
            availableBalance = latestReportedBalance,
            dailyGroups = dailyGroups,
            summary = summary,
            scanProgress = scanProgress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun startInboxScan(context: Context) {
        if (scanProgressFlow.value.isScanning) return
        viewModelScope.launch {
            val config = uiState.value.accountConfig
            scanner.scanInbox(context, config) { progress ->
                scanProgressFlow.value = progress
            }
        }
    }

    fun saveAccountConfig(config: BankAccountConfig) {
        viewModelScope.launch {
            repository.saveMonitoredAccountConfig(config)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val db = HermoditDatabase.getInstance(application)
                val dataSource = RoomTransactionDataSource(db.transactionDao())
                val repository = TransactionRepositoryImpl(
                    localDataSource = dataSource,
                    configDao = db.bankAccountConfigDao(),
                    unmatchedSmsDao = db.unmatchedSmsDao()
                )
                val getDailyGroupsUseCase = GetDailyTransactionGroupsUseCase(repository)
                val getFinancialSummaryUseCase = GetFinancialSummaryUseCase(repository)
                return DashboardViewModel(
                    getDailyGroupsUseCase = getDailyGroupsUseCase,
                    getFinancialSummaryUseCase = getFinancialSummaryUseCase,
                    repository = repository
                ) as T
            }
        }
    }
}

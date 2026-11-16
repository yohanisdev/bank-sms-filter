package com.example.hermodit.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hermodit.data.datasource.InMemoryTransactionDataSource
import com.example.hermodit.data.repository.TransactionRepositoryImpl
import com.example.hermodit.domain.model.BankAccountConfig
import com.example.hermodit.domain.repository.TransactionRepository
import com.example.hermodit.domain.usecase.GetDailyTransactionGroupsUseCase
import com.example.hermodit.domain.usecase.GetFinancialSummaryUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing state for the Main Dashboard screen.
 */
class DashboardViewModel(
    private val getDailyGroupsUseCase: GetDailyTransactionGroupsUseCase,
    private val getFinancialSummaryUseCase: GetFinancialSummaryUseCase,
    val repository: TransactionRepository
) : ViewModel() {

    private val isSyncingFlow = MutableStateFlow(false)
    private val accountConfigFlow = MutableStateFlow(BankAccountConfig())

    val uiState: StateFlow<DashboardUiState> = combine(
        getDailyGroupsUseCase(),
        getFinancialSummaryUseCase(),
        isSyncingFlow,
        accountConfigFlow
    ) { dailyGroups, summary, isSyncing, accountConfig ->
        // Use latest reported balance from the most recent daily group that reported a balance
        val latestReportedBalance = dailyGroups
            .firstOrNull { it.summary.latestReportedBalance != null }
            ?.summary
            ?.latestReportedBalance

        DashboardUiState(
            isLoading = false,
            isSyncing = isSyncing,
            accountConfig = accountConfig,
            availableBalance = latestReportedBalance,
            dailyGroups = dailyGroups,
            summary = summary
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun onSyncNowClick() {
        if (isSyncingFlow.value) return
        viewModelScope.launch {
            isSyncingFlow.value = true
            // Simulate safe offline sync cycle for UI verification
            delay(1200)
            isSyncingFlow.value = false
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val dataSource = InMemoryTransactionDataSource()
                val repository = TransactionRepositoryImpl(dataSource)
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

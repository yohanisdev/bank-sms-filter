package com.example.hermodit.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.LocalDate

/**
 * Lightweight, zero-dependency, backstack-aware navigator for Hermodit.
 */
class HermoditNavigator(initialScreen: Screen = Screen.Dashboard) {
    private val backStack = mutableStateListOf<Screen>(initialScreen)

    var currentScreen: Screen by mutableStateOf(initialScreen)
        private set

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            backStack.add(screen)
            currentScreen = screen
        }
    }

    fun navigateToDailyDetail(date: LocalDate) {
        navigateTo(Screen.DailyDetail(date))
    }

    fun navigateToTransactionDetail(transactionId: String) {
        navigateTo(Screen.TransactionDetail(transactionId))
    }

    fun navigateToAccountConfig() {
        navigateTo(Screen.AccountConfig)
    }

    fun navigateToSettings() {
        navigateTo(Screen.Settings)
    }

    fun navigateToDashboard() {
        backStack.clear()
        backStack.add(Screen.Dashboard)
        currentScreen = Screen.Dashboard
    }

    fun popBack(): Boolean {
        return if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
            currentScreen = backStack.last()
            true
        } else {
            false
        }
    }
}

@Composable
fun rememberHermoditNavigator(initialScreen: Screen = Screen.Dashboard): HermoditNavigator {
    return remember { HermoditNavigator(initialScreen) }
}

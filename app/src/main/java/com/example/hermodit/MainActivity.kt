package com.example.hermodit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.hermodit.presentation.dashboard.DashboardViewModel
import com.example.hermodit.presentation.navigation.HermoditNavHost
import com.example.hermodit.ui.theme.HermoditTheme

class MainActivity : ComponentActivity() {
    private val dashboardViewModel: DashboardViewModel by viewModels { DashboardViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HermoditTheme {
                HermoditNavHost(
                    viewModel = dashboardViewModel
                )
            }
        }
    }
}

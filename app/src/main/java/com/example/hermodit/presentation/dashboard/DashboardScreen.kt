package com.example.hermodit.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hermodit.presentation.components.AccountOverviewHeroCard
import com.example.hermodit.presentation.components.DailyGroupCard
import com.example.hermodit.presentation.components.HermoditBottomNav
import com.example.hermodit.presentation.components.HermoditTopAppBar
import com.example.hermodit.presentation.navigation.Screen
import com.example.hermodit.presentation.theme.HermoditColors
import com.example.hermodit.ui.theme.HermoditTheme
import java.time.LocalDate

/**
 * Screen 1: Main Dashboard Screen matching Stitch specifications.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToDailyDetail: (LocalDate) -> Unit,
    onNavigateToAccountConfig: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            HermoditTopAppBar(
                onSyncClick = viewModel::onSyncNowClick,
                onSettingsClick = onNavigateToSettings
            )
        },
        bottomBar = {
            HermoditBottomNav(
                currentScreen = Screen.Dashboard,
                onDashboardClick = { /* Already on dashboard */ },
                onTransactionsClick = {
                    uiState.dailyGroups.firstOrNull()?.summary?.date?.let(onNavigateToDailyDetail)
                },
                onSettingsClick = onNavigateToSettings
            )
        },
        containerColor = HermoditColors.CanvasBackground
    ) { innerPadding ->
        DashboardContent(
            uiState = uiState,
            onSyncClick = viewModel::onSyncNowClick,
            onNavigateToDailyDetail = onNavigateToDailyDetail,
            onNavigateToAccountConfig = onNavigateToAccountConfig,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onSyncClick: () -> Unit,
    onNavigateToDailyDetail: (LocalDate) -> Unit,
    onNavigateToAccountConfig: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = HermoditColors.Primary)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Local Sync Verification Strip
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(HermoditColors.SurfaceCard)
                    .border(1.dp, HermoditColors.OutlineVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🛡",
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                        Text(
                            text = "Zero Remote Calls • 100% On-Device SMS Parsing",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = HermoditColors.TextPrimary,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF0F9FF))
                            .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "v2.4-OFFLINE",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = HermoditColors.Primary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Account Overview Hero Card
        item {
            AccountOverviewHeroCard(
                accountConfig = uiState.accountConfig,
                availableBalance = uiState.availableBalance,
                onSyncNowClick = onSyncClick,
                onAccountClick = onNavigateToAccountConfig,
                isSyncing = uiState.isSyncing
            )
        }

        // Quick Filter Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFF0F9FF))
                            .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "All CBE Inboxes",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = HermoditColors.Primary,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(HermoditColors.SurfaceCard)
                            .border(1.dp, HermoditColors.OutlineVariant, RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Telebirr (Off)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = HermoditColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                TextButton(onClick = { /* Export CSV action */ }) {
                    Text(
                        text = "Export CSV",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = HermoditColors.Primary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Section Header: Daily Transaction Groups
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Transaction Groups",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = HermoditColors.TextPrimary
                    )
                    Text(
                        text = "Organized deterministically from parsed bank SMS",
                        style = MaterialTheme.typography.bodySmall,
                        color = HermoditColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(HermoditColors.SurfaceCard)
                        .border(1.dp, HermoditColors.OutlineVariant, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${uiState.dailyGroups.size} Days Active",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = HermoditColors.TextPrimary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Daily Groups List (Newest to Oldest)
        items(
            items = uiState.dailyGroups,
            key = { it.summary.date.toString() }
        ) { group ->
            val isToday = group.summary.date == LocalDate.of(2026, 11, 16)
            DailyGroupCard(
                group = group,
                onClick = { onNavigateToDailyDetail(group.summary.date) },
                isToday = isToday
            )
        }

        // Offline SMS Telemetry Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(HermoditColors.SurfaceCard)
                    .border(1.dp, HermoditColors.OutlineVariant, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OFFLINE SMS TELEMETRY SAMPLE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = HermoditColors.TextSecondary,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(HermoditColors.InflowEmeraldContainer)
                                .border(1.dp, HermoditColors.InflowEmeraldBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MATCH 100%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HermoditColors.InflowEmerald,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, HermoditColors.OutlineVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Dear Customer, ETB 5,000.00 transferred to your CBE Account ...4582. Balance ETB 25,000.00. Ref: FT26321.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = HermoditColors.TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    val sampleTxs = com.example.hermodit.data.datasource.InMemoryTransactionDataSource.createSampleTransactions()
    val groups = listOf(
        com.example.hermodit.domain.model.DailyTransactionGroup(
            summary = com.example.hermodit.domain.model.DailySummary(
                date = LocalDate.of(2026, 11, 16),
                totalReceived = java.math.BigDecimal("6000.00"),
                totalDeposited = java.math.BigDecimal("2500.00"),
                totalWithdrawn = java.math.BigDecimal("3500.00"),
                netMovement = java.math.BigDecimal("5000.00"),
                latestReportedBalance = java.math.BigDecimal("25000.00"),
                transactionCount = 10
            ),
            transactions = sampleTxs.filter { it.transactionDate == LocalDate.of(2026, 11, 16) }
        ),
        com.example.hermodit.domain.model.DailyTransactionGroup(
            summary = com.example.hermodit.domain.model.DailySummary(
                date = LocalDate.of(2026, 11, 15),
                totalReceived = java.math.BigDecimal("4000.00"),
                totalDeposited = java.math.BigDecimal.ZERO,
                totalWithdrawn = java.math.BigDecimal("1000.00"),
                netMovement = java.math.BigDecimal("3000.00"),
                latestReportedBalance = java.math.BigDecimal("20000.00"),
                transactionCount = 7
            ),
            transactions = sampleTxs.filter { it.transactionDate == LocalDate.of(2026, 11, 15) }
        ),
        com.example.hermodit.domain.model.DailyTransactionGroup(
            summary = com.example.hermodit.domain.model.DailySummary(
                date = LocalDate.of(2026, 11, 14),
                totalReceived = java.math.BigDecimal.ZERO,
                totalDeposited = java.math.BigDecimal.ZERO,
                totalWithdrawn = java.math.BigDecimal("2150.00"),
                netMovement = java.math.BigDecimal("-2150.00"),
                latestReportedBalance = java.math.BigDecimal("17000.00"),
                transactionCount = 4
            ),
            transactions = sampleTxs.filter { it.transactionDate == LocalDate.of(2026, 11, 14) }
        )
    )

    HermoditTheme {
        DashboardContent(
            uiState = DashboardUiState(
                isLoading = false,
                availableBalance = java.math.BigDecimal("25000.00"),
                dailyGroups = groups
            ),
            onSyncClick = {},
            onNavigateToDailyDetail = {},
            onNavigateToAccountConfig = {}
        )
    }
}

package com.example.hermodit.presentation.dashboard

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.hermodit.R
import com.example.hermodit.domain.model.DailyTransactionGroup
import com.example.hermodit.presentation.components.AccountOverviewHeroCard
import com.example.hermodit.presentation.components.DailyGroupCard
import com.example.hermodit.presentation.components.HermoditBottomNav
import com.example.hermodit.presentation.components.HermoditTopAppBar
import com.example.hermodit.presentation.navigation.Screen
import com.example.hermodit.presentation.theme.HermoditColors
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
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startInboxScan(context)
        } else {
            Toast.makeText(
                context,
                "SMS permission is required to scan bank notifications",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val triggerSync = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.startInboxScan(context)
        } else {
            permissionLauncher.launch(android.Manifest.permission.READ_SMS)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            HermoditTopAppBar(
                onSyncClick = triggerSync,
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
            onSyncClick = triggerSync,
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

    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredGroups = remember(uiState.dailyGroups, selectedFilter) {
        when (selectedFilter) {
            "TELEBIRR" -> {
                uiState.dailyGroups.mapNotNull { group ->
                    val telebirrTxs = group.transactions.filter {
                        it.channel == "TELEBIRR" || it.counterpartyName?.contains("telebirr", ignoreCase = true) == true
                    }
                    if (telebirrTxs.isNotEmpty()) {
                        DailyTransactionGroup(
                            summary = group.summary.copy(transactionCount = telebirrTxs.size),
                            transactions = telebirrTxs
                        )
                    } else null
                }
            }
            else -> uiState.dailyGroups
        }
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
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shield),
                            contentDescription = "Security",
                            tint = HermoditColors.Primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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
                            .background(HermoditColors.PrimaryLight)
                            .border(1.dp, HermoditColors.PrimaryBorder, RoundedCornerShape(4.dp))
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

        // Live Scanning Progress / Audit Bar (if active or just scanned)
        if (uiState.isSyncing || uiState.scanProgress.scannedCount > 0) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(HermoditColors.SurfaceNested)
                        .border(1.dp, HermoditColors.PrimaryBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = HermoditColors.Primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Scanning SMS Inbox...",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = HermoditColors.Primary
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_check_circle),
                                        contentDescription = "Completed",
                                        tint = HermoditColors.InflowEmerald,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Inbox Scan Completed",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = HermoditColors.InflowEmerald
                                    )
                                }
                            }

                            Text(
                                text = "Saved: ${uiState.scanProgress.savedCount} txs",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = HermoditColors.TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Scanned: ${uiState.scanProgress.scannedCount} • Matched: ${uiState.scanProgress.matchedCount} • Duplicates: ${uiState.scanProgress.duplicateCount} • Ignored: ${uiState.scanProgress.ignoredCount} • Ambiguous: ${uiState.scanProgress.ambiguousCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = HermoditColors.TextSecondary,
                            fontSize = 11.sp
                        )

                        uiState.scanProgress.errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = err,
                                style = MaterialTheme.typography.labelSmall,
                                color = HermoditColors.OutflowCrimson,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Quick Filter Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isAll = selectedFilter == "ALL"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isAll) HermoditColors.PrimaryLight else HermoditColors.SurfaceCard)
                            .border(
                                1.dp,
                                if (isAll) HermoditColors.PrimaryBorder else HermoditColors.OutlineVariant,
                                RoundedCornerShape(50)
                            )
                            .clickable { selectedFilter = "ALL" }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_filter_alt),
                                contentDescription = "Filter",
                                tint = if (isAll) HermoditColors.Primary else HermoditColors.TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "All CBE Inboxes",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAll) HermoditColors.Primary else HermoditColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    val isTelebirr = selectedFilter == "TELEBIRR"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isTelebirr) HermoditColors.PrimaryLight else HermoditColors.SurfaceCard)
                            .border(
                                1.dp,
                                if (isTelebirr) HermoditColors.PrimaryBorder else HermoditColors.OutlineVariant,
                                RoundedCornerShape(50)
                            )
                            .clickable { selectedFilter = if (isTelebirr) "ALL" else "TELEBIRR" }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isTelebirr) "Telebirr (Active)" else "Telebirr",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isTelebirr) FontWeight.Bold else FontWeight.Medium,
                            color = if (isTelebirr) HermoditColors.Primary else HermoditColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                TextButton(onClick = { /* Export action */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_file_download),
                        contentDescription = "Export CSV",
                        tint = HermoditColors.Primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
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
                        text = "${filteredGroups.size} Days Active",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = HermoditColors.TextPrimary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Empty state when Room database has no transactions yet
        if (filteredGroups.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(HermoditColors.SurfaceCard)
                        .border(1.dp, HermoditColors.OutlineVariant, RoundedCornerShape(16.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_receipt_long),
                            contentDescription = "No transactions",
                            tint = HermoditColors.Outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Parsed Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HermoditColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No SMS notifications found for monitored account 1000763003608. Tap 'Sync Now' above to scan your SMS inbox.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HermoditColors.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Daily Groups List (Newest to Oldest)
            items(
                items = filteredGroups,
                key = { it.summary.date.toString() }
            ) { group ->
                val isToday = group.summary.date == LocalDate.now()
                DailyGroupCard(
                    group = group,
                    onClick = { onNavigateToDailyDetail(group.summary.date) },
                    isToday = isToday
                )
            }
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
                            .background(HermoditColors.SurfaceLow)
                            .border(1.dp, HermoditColors.OutlineVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Dear Customer, ETB 5,000.00 transferred to your CBE Account ...3608. Balance ETB 25,000.00. Ref: FT26321.",
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

package com.example.hermodit.presentation.settings

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hermodit.domain.model.SyncSettings
import com.example.hermodit.presentation.components.HermoditBottomNav
import com.example.hermodit.presentation.navigation.Screen
import com.example.hermodit.presentation.theme.HermoditColors

/**
 * Screen 5: Settings & Synchronization Engine Screen matching Stitch specifications.
 */
@Composable
fun SettingsScreen(
    initialSettings: SyncSettings = SyncSettings(),
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    var autoSyncEnabled by remember { mutableStateOf(initialSettings.automaticSyncEnabled) }
    var oditApiEnabled by remember { mutableStateOf(initialSettings.oditApiIntegrationEnabled) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HermoditColors.SurfaceCard,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text(
                            text = "←",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = HermoditColors.TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HermoditColors.TextPrimary
                        )
                        Text(
                            text = "Preferences & On-Device Engine",
                            style = MaterialTheme.typography.bodySmall,
                            color = HermoditColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        bottomBar = {
            HermoditBottomNav(
                currentScreen = Screen.Settings,
                onDashboardClick = onNavigateToDashboard,
                onTransactionsClick = onNavigateToTransactions,
                onSettingsClick = { /* Already on settings */ }
            )
        },
        containerColor = HermoditColors.CanvasBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION 1: Synchronization Engine
            item {
                Text(
                    text = "SYNCHRONIZATION ENGINE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = HermoditColors.Primary,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HermoditColors.SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HermoditColors.OutlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column {
                        // Item: Automatic SMS Synchronization
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Automatic SMS Synchronization",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.TextPrimary
                                )
                                Text(
                                    text = "Parse bank notifications immediately upon reception",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = autoSyncEnabled,
                                onCheckedChange = { autoSyncEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HermoditColors.Primary)
                            )
                        }

                        SettingsDivider()

                        // Item: Last Successful Sync
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Last Successful Sync",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.TextPrimary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(HermoditColors.InflowEmerald)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = initialSettings.lastSuccessfulSync,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HermoditColors.InflowEmerald,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "3 mins ago",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        SettingsDivider()

                        // Item: SMS Background Worker
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SMS Background Worker",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.TextPrimary
                                )
                                Text(
                                    text = "Active (Android WorkManager)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(HermoditColors.InflowEmeraldContainer)
                                    .border(1.dp, HermoditColors.InflowEmeraldBorder, RoundedCornerShape(50))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Battery Optimized",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HermoditColors.InflowEmerald,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        SettingsDivider()

                        // Item: Sync Retries
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Sync Retries on Delivery Failures",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.TextPrimary
                                )
                                Text(
                                    text = "Exponential backoff schedule",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF0F9FF))
                                    .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "3 Attempts",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HermoditColors.Primary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 2: Future Odit API Integration
            item {
                Text(
                    text = "EXTERNAL API BOUNDARY (ODIT)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = HermoditColors.Primary,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HermoditColors.SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HermoditColors.OutlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Odit Financial API Boundary",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.TextPrimary
                                )
                                Text(
                                    text = "Prepare integration boundary for external Odit consumer",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = oditApiEnabled,
                                onCheckedChange = { oditApiEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HermoditColors.Primary)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, HermoditColors.OutlineVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Configured Endpoint Placeholder:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = initialSettings.oditApiEndpoint,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = HermoditColors.TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Status: Awaiting external API specification • Zero network transmission in this phase.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HermoditColors.TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // SECTION 3: Storage & Privacy
            item {
                Text(
                    text = "STORAGE & PRIVACY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = HermoditColors.Primary,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HermoditColors.SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HermoditColors.OutlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "On-Device Storage Engine",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = HermoditColors.TextPrimary
                            )
                            Text(
                                text = "Encrypted SQLite",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = HermoditColors.TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Telemetry & Analytics",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = HermoditColors.TextPrimary
                            )
                            Text(
                                text = "Disabled (100% Offline)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HermoditColors.InflowEmerald
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { /* Clear cache UI only */ },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Clear Parse Cache (Safe)",
                                color = HermoditColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFF1F5F9))
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    com.example.hermodit.ui.theme.HermoditTheme {
        SettingsScreen(
            onNavigateBack = {},
            onNavigateToDashboard = {},
            onNavigateToTransactions = {}
        )
    }
}

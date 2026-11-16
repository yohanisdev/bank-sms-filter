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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hermodit.R
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
    onSyncNowClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    var autoSyncEnabled by remember { mutableStateOf(initialSettings.automaticSyncEnabled) }
    var selectedTheme by remember { mutableStateOf("Light (System Default)") }
    var decimalPrecision by remember { mutableStateOf("2 Decimal Places") }
    var numberGrouping by remember { mutableStateOf("1,234.56 (Comma)") }

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
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = HermoditColors.TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HermoditColors.TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(HermoditColors.InflowEmerald)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "OFFLINE ENGINE ACTIVE",
                                style = MaterialTheme.typography.bodySmall,
                                color = HermoditColors.InflowEmeraldDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onSyncNowClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sync),
                            contentDescription = "Sync",
                            tint = HermoditColors.Primary,
                            modifier = Modifier.size(20.dp)
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
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = HermoditColors.Primary
                                )
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
                                        color = HermoditColors.InflowEmeraldDark,
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
                                    text = "Real-Time / Ready",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        SettingsDivider()

                        // Item: SMS Background Engine
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Broadcast Receiver",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.TextPrimary
                                )
                                Text(
                                    text = "Registered in AndroidManifest",
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
                                    text = "Active Listener",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HermoditColors.InflowEmeraldDark,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 2: Appearance & Display
            item {
                Text(
                    text = "APPEARANCE & DISPLAY",
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
                        // Theme Preference
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "App Theme",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.TextPrimary
                                )
                                Text(
                                    text = "Stitch Clean Light theme",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = selectedTheme,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.TextPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        SettingsDivider()

                        // Currency Display
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Currency Display",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.TextPrimary
                                )
                                Text(
                                    text = "ISO 4217 standard symbol",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HermoditColors.PrimaryContainer)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ETB (Ethiopian Birr)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HermoditColors.Primary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        SettingsDivider()

                        // Decimal Precision
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Decimal Precision",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.TextPrimary
                                )
                                Text(
                                    text = "Display cents in all transaction lists",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = decimalPrecision,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.TextPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        SettingsDivider()

                        // Number Grouping Format
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Number Grouping Format",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.TextPrimary
                                )
                                Text(
                                    text = "Standard financial thousand separators",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = numberGrouping,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.TextPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
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
                                text = "Room SQLite Database",
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
                                color = HermoditColors.InflowEmeraldDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monitored Account Policy",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = HermoditColors.TextPrimary
                            )
                            Text(
                                text = "Single Account (1000763003608)",
                                style = MaterialTheme.typography.labelSmall,
                                color = HermoditColors.TextSecondary
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

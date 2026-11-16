package com.example.hermodit.presentation.config

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.hermodit.R
import com.example.hermodit.domain.model.BankAccountConfig
import com.example.hermodit.presentation.theme.HermoditColors

/**
 * Screen 4: Bank Account Configuration Screen matching Stitch specifications.
 * Configured specifically for Commercial Bank of Ethiopia (CBE) account: 1000763003608.
 */
@Composable
fun AccountConfigScreen(
    initialConfig: BankAccountConfig,
    onNavigateBack: () -> Unit,
    onSaveConfig: (BankAccountConfig) -> Unit = {},
    onStartScan: () -> Unit = {},
    isScanning: Boolean = false,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)
    val context = LocalContext.current

    var selectedBank by remember { mutableStateOf(initialConfig.bankName) }
    var accountHolder by remember { mutableStateOf(initialConfig.accountHolder) }
    var accountInput by remember { mutableStateOf(initialConfig.fullAccountNumber) }
    var isRevealed by remember { mutableStateOf(false) }
    val senderIds = remember { mutableStateListOf(*initialConfig.authorizedSenderIds.toTypedArray()) }
    var backgroundListening by remember { mutableStateOf(true) }
    var syncScope by remember { mutableStateOf("Full Inbox Sync") }
    var isSaved by remember { mutableStateOf(false) }
    var showAddSenderDialog by remember { mutableStateOf(false) }
    var newSenderInput by remember { mutableStateOf("") }

    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasSmsPermission = isGranted
        if (isGranted) {
            onStartScan()
        }
    }

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

                    Text(
                        text = "Bank Account Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HermoditColors.TextPrimary
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HermoditColors.SurfaceCard,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, HermoditColors.OutlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HermoditColors.TextPrimary)
                    ) {
                        Text(text = "Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val updatedConfig = initialConfig.copy(
                                bankName = selectedBank,
                                fullAccountNumber = accountInput.trim(),
                                accountIdentifier = if (accountInput.length >= 4) "•••• ${accountInput.takeLast(4)}" else accountInput,
                                accountHolder = accountHolder.trim(),
                                authorizedSenderIds = senderIds.toList()
                            )
                            onSaveConfig(updatedConfig)
                            isSaved = true
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HermoditColors.Primary)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSaved) "Saved ✓" else "Save Configuration",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
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
            // Section 1: Monitored Bank Selection
            item {
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
                                text = "Monitored Financial Institution",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HermoditColors.TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(HermoditColors.InflowEmeraldContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Active Parser",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.InflowEmerald,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Active bank selection row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF0F9FF))
                                .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HermoditColors.Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "CBE",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedBank,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.TextPrimary
                                )
                                Text(
                                    text = "SMS Gateways: CBE, CBE_Birr, 889, telebirr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check_circle),
                                contentDescription = "Selected",
                                tint = HermoditColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Supported bank formats:",
                            style = MaterialTheme.typography.labelSmall,
                            color = HermoditColors.TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("Commercial Bank of Ethiopia (CBE)", "Telebirr via CBE", "Awash Bank (Upcoming)")) { bank ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .border(1.dp, HermoditColors.OutlineVariant, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = bank,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HermoditColors.TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Monitored Account Identifier & Holder
            item {
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
                                text = "Monitored Account Identifier",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HermoditColors.TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HermoditColors.InflowEmeraldContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Strict Matching Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HermoditColors.InflowEmerald,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Account Holder Name Field
                        Text(
                            text = "Account Holder(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = HermoditColors.TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = accountHolder,
                            onValueChange = { accountHolder = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HermoditColors.Primary,
                                unfocusedBorderColor = HermoditColors.Outline
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Account Number Field
                        Text(
                            text = "Account Number",
                            style = MaterialTheme.typography.labelSmall,
                            color = HermoditColors.TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = if (isRevealed) accountInput else (if (accountInput.length >= 4) "•••• ${accountInput.takeLast(4)}" else accountInput),
                            onValueChange = { accountInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HermoditColors.Primary,
                                unfocusedBorderColor = HermoditColors.Outline
                            ),
                            trailingIcon = {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .clickable { isRevealed = !isRevealed }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isRevealed) "Hide" else "Reveal Full",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = HermoditColors.TextPrimary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Used to filter incoming SMS notifications specifically to this account. Messages for other accounts are ignored.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HermoditColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Section 3: Authorized SMS Sender IDs
            item {
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
                            Column {
                                Text(
                                    text = "Authorized SMS Sender IDs",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HermoditColors.TextPrimary
                                )
                                Text(
                                    text = "Hermodit ignores SMS from unregistered headers",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFF0F9FF))
                                    .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(50))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${senderIds.size} Whitelisted",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HermoditColors.Primary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(senderIds) { sender ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF0F9FF))
                                        .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(HermoditColors.Primary)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = sender,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = HermoditColors.Primary,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_close),
                                            contentDescription = "Remove",
                                            tint = HermoditColors.TextSecondary,
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clickable { senderIds.remove(sender) }
                                        )
                                    }
                                }
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HermoditColors.PrimaryContainer)
                                        .clickable { showAddSenderDialog = true }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_add),
                                            contentDescription = null,
                                            tint = HermoditColors.Primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Add Sender ID",
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
                }
            }

            // Section 4: SMS Permission & Engine Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HermoditColors.SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HermoditColors.OutlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SMS Permission & Engine Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HermoditColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Permission status item
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Android SMS Permission (READ_SMS)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = HermoditColors.TextPrimary
                                )
                                Text(
                                    text = if (hasSmsPermission) "Authorized by Android system" else "Requires user authorization",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (hasSmsPermission) HermoditColors.InflowEmeraldContainer else Color(0xFFFEF2F2))
                                    .border(
                                        1.dp,
                                        if (hasSmsPermission) HermoditColors.InflowEmeraldBorder else Color(0xFFFECACA),
                                        RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (hasSmsPermission) "GRANTED" else "NOT GRANTED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasSmsPermission) HermoditColors.InflowEmeraldDark else HermoditColors.OutflowCrimson,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Background listening switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Real-Time Incoming Monitoring",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = HermoditColors.TextPrimary
                                )
                                Text(
                                    text = "Automatically capture new bank SMS via system broadcast",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = backgroundListening,
                                onCheckedChange = { backgroundListening = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = HermoditColors.Primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sync scope selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Historical Sync Scope",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = HermoditColors.TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .clickable {
                                        syncScope = if (syncScope == "Full Inbox Sync") "Last 30 Days" else "Full Inbox Sync"
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = syncScope,
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

            // Section 5: Historical Synchronization Action Card
            item {
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
                            Column {
                                Text(
                                    text = "Historical SMS Synchronization",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HermoditColors.TextPrimary
                                )
                                Text(
                                    text = "Scan existing SMS messages for account 1000763003608",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(
                                painter = painterResource(id = R.drawable.ic_sync),
                                contentDescription = null,
                                tint = HermoditColors.Primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (hasSmsPermission) {
                                    onStartScan()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.READ_SMS)
                                }
                            },
                            enabled = !isScanning,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HermoditColors.Primary)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_sync),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isScanning) "Scanning Inbox..." else if (hasSmsPermission) "Scan SMS Inbox Now" else "Request Permission & Scan",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSenderDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddSenderDialog = false
                newSenderInput = ""
            },
            title = {
                Text(text = "Add Authorized Sender ID", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Enter SMS sender header (e.g. CBE, 889, telebirr):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HermoditColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newSenderInput,
                        onValueChange = { newSenderInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. telebirr") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newSenderInput.trim()
                        if (trimmed.isNotEmpty() && !senderIds.contains(trimmed)) {
                            senderIds.add(trimmed)
                        }
                        showAddSenderDialog = false
                        newSenderInput = ""
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddSenderDialog = false
                        newSenderInput = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AccountConfigScreenPreview() {
    com.example.hermodit.ui.theme.HermoditTheme {
        AccountConfigScreen(
            initialConfig = BankAccountConfig(),
            onNavigateBack = {}
        )
    }
}

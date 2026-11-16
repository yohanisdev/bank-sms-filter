package com.example.hermodit.presentation.txdetail

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hermodit.core.util.CurrencyFormatter
import com.example.hermodit.core.util.DateTimeUtils
import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.model.TransactionType
import com.example.hermodit.domain.repository.TransactionRepository
import com.example.hermodit.presentation.components.RawSmsAuditBox
import com.example.hermodit.presentation.theme.HermoditColors

/**
 * Screen 3: Transaction Details & Raw SMS Audit Screen matching Stitch specifications.
 */
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    repository: TransactionRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    val transaction = produceState<FinancialTransaction?>(initialValue = null, key1 = transactionId) {
        value = repository.getTransactionById(transactionId)
    }.value

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

                    Text(
                        text = "Transaction Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HermoditColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { /* Share or Export */ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "↗",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = HermoditColors.TextSecondary
                        )
                    }
                }
            }
        },
        containerColor = HermoditColors.CanvasBackground
    ) { innerPadding ->
        if (transaction == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading transaction details...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HermoditColors.TextSecondary
                )
            }
        } else {
            val isInflow = transaction.type == TransactionType.RECEIVED || transaction.type == TransactionType.DEPOSIT
            val amountColor = if (isInflow) HermoditColors.InflowEmerald else HermoditColors.OutflowCrimson

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero Card: Amount and Verification
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = HermoditColors.SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HermoditColors.OutlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Badge Icon
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (isInflow) HermoditColors.InflowEmeraldContainer else HermoditColors.OutflowCrimsonContainer)
                                    .border(
                                        1.dp,
                                        if (isInflow) HermoditColors.InflowEmeraldBorder else HermoditColors.OutflowCrimsonBorder,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isInflow) "↓" else "↑",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = amountColor
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = when (transaction.type) {
                                    TransactionType.RECEIVED -> "Direct Bank Transfer Received"
                                    TransactionType.DEPOSIT -> "Cash Deposit Received"
                                    TransactionType.WITHDRAWAL -> "Bank Debit / Withdrawal"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = amountColor,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Large Amount
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${if (isInflow) "+" else "-"}${CurrencyFormatter.formatAmountOnly(transaction.amount)}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 34.sp,
                                    color = amountColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = transaction.currency,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = amountColor,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Formatted Timestamp
                            val timeStr = transaction.transactionTime?.let {
                                val hour = if (it.hour % 12 == 0) 12 else it.hour % 12
                                val amPm = if (it.hour < 12) "AM" else "PM"
                                String.format(java.util.Locale.US, "%02d:%02d %s", hour, it.minute, amPm)
                            } ?: DateTimeUtils.formatTime(transaction.smsTimestamp)

                            Text(
                                text = "${DateTimeUtils.formatDate(transaction.smsTimestamp)} at $timeStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = HermoditColors.TextSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Verified Status Tag Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFF0F9FF))
                                    .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🛡", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SMS Verified & Parsed",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = HermoditColors.Primary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Detailed Information Grid
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HermoditColors.SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HermoditColors.OutlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                    ) {
                        Column {
                            DetailRow(label = "Transaction Type", value = transaction.type.name)
                            DetailRowDivider()
                            DetailRow(label = "Bank", value = transaction.bankName)
                            DetailRowDivider()
                            DetailRow(
                                label = if (isInflow) "Sender" else "Recipient",
                                value = transaction.counterpartyName ?: "Not specified"
                            )
                            DetailRowDivider()
                            DetailRow(label = "Account Identifier", value = transaction.accountIdentifier, isMono = true)
                            DetailRowDivider()
                            DetailRow(
                                label = "Transaction Reference",
                                value = transaction.bankReferenceNumber ?: "None",
                                isMono = true,
                                isAccent = true
                            )
                            DetailRowDivider()
                            DetailRow(
                                label = "Available Balance",
                                value = transaction.availableBalance?.let { "${CurrencyFormatter.formatAmountOnly(it)} ETB" } ?: "Not reported by bank",
                                isMono = true
                            )
                            DetailRowDivider()
                            DetailRow(label = "Channel / Method", value = "Mobile / Core Banking SMS")
                        }
                    }
                }

                // Raw SMS Audit Section
                item {
                    RawSmsAuditBox(
                        rawSmsContent = transaction.rawSmsContent,
                        initialExpanded = false
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isMono: Boolean = false,
    isAccent: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = HermoditColors.TextSecondary,
            fontSize = 13.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isAccent) FontWeight.Bold else FontWeight.Medium,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
            color = if (isAccent) HermoditColors.Primary else HermoditColors.TextPrimary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun DetailRowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFF1F5F9))
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun TransactionDetailScreenPreview() {
    com.example.hermodit.ui.theme.HermoditTheme {
        TransactionDetailScreen(
            transactionId = "tx-101",
            repository = com.example.hermodit.data.repository.TransactionRepositoryImpl(
                com.example.hermodit.data.datasource.InMemoryTransactionDataSource()
            ),
            onNavigateBack = {}
        )
    }
}

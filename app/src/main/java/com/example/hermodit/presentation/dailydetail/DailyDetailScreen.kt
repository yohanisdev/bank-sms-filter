package com.example.hermodit.presentation.dailydetail

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hermodit.core.util.CurrencyFormatter
import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.model.TransactionType
import com.example.hermodit.domain.repository.TransactionRepository
import com.example.hermodit.presentation.components.TransactionRowCard
import com.example.hermodit.presentation.theme.HermoditColors
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Screen 2: Daily Transaction Details Screen matching Stitch specifications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyDetailScreen(
    date: LocalDate,
    repository: TransactionRepository,
    onNavigateBack: () -> Unit,
    onNavigateToTransactionDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    val sampleInitial = remember(date) {
        com.example.hermodit.data.datasource.InMemoryTransactionDataSource.createSampleTransactions()
            .filter { it.transactionDate == date }
    }
    val transactions by repository.getTransactionsForDate(date).collectAsState(initial = sampleInitial)
    var selectedFilter by remember { mutableStateOf<TransactionType?>(null) }

    val filteredTransactions = remember(transactions, selectedFilter) {
        if (selectedFilter == null) {
            transactions
        } else {
            transactions.filter { it.type == selectedFilter }
        }
    }

    // Ledger totals
    val totalReceived = remember(transactions) {
        transactions.filter { it.type == TransactionType.RECEIVED }
            .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }
    }
    val totalDeposited = remember(transactions) {
        transactions.filter { it.type == TransactionType.DEPOSIT }
            .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }
    }
    val totalWithdrawn = remember(transactions) {
        transactions.filter { it.type == TransactionType.WITHDRAWAL }
            .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }
    }
    val netMovement = remember(totalReceived, totalDeposited, totalWithdrawn) {
        totalReceived.add(totalDeposited).subtract(totalWithdrawn)
    }
    val closingBalance = remember(transactions) {
        transactions.firstOrNull { it.availableBalance != null }?.availableBalance
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
                        Text(
                            text = "←",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = HermoditColors.TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatHeaderDate(date),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HermoditColors.TextPrimary
                        )
                        Text(
                            text = "${transactions.size} Parsed Transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = HermoditColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // Share or Export action
                    IconButton(
                        onClick = { /* Export action */ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "⤓",
                            fontSize = 20.sp,
                            color = HermoditColors.TextSecondary
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
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Daily Summary Card
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
                            .padding(18.dp)
                    ) {
                        // Net Movement Hero Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "NET MOVEMENT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HermoditColors.TextSecondary,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    val isNetPos = netMovement >= BigDecimal.ZERO
                                    val netColor = if (isNetPos) HermoditColors.InflowEmerald else HermoditColors.OutflowCrimson
                                    Text(
                                        text = "${if (isNetPos) "+" else ""}${CurrencyFormatter.formatAmountOnly(netMovement)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = netColor
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ETB",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = netColor,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }

                            val isNetPos = netMovement >= BigDecimal.ZERO
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isNetPos) HermoditColors.InflowEmeraldContainer else HermoditColors.OutflowCrimsonContainer)
                                    .border(
                                        1.dp,
                                        if (isNetPos) HermoditColors.InflowEmeraldBorder else HermoditColors.OutflowCrimsonBorder,
                                        RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isNetPos) "Net positive" else "Net negative",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNetPos) HermoditColors.InflowEmerald else HermoditColors.OutflowCrimson,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3-Column Ledger Breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Column 1: Received / Deposited
                            val totalIn = totalReceived.add(totalDeposited)
                            LedgerColumnBox(
                                label = "Received",
                                amount = CurrencyFormatter.formatAmountOnly(totalIn),
                                color = HermoditColors.InflowEmerald,
                                arrow = "↙",
                                modifier = Modifier.weight(1f)
                            )

                            // Column 2: Withdrawn
                            LedgerColumnBox(
                                label = "Withdrawn",
                                amount = CurrencyFormatter.formatAmountOnly(totalWithdrawn),
                                color = HermoditColors.OutflowCrimson,
                                arrow = "↗",
                                modifier = Modifier.weight(1f)
                            )

                            // Column 3: Closing Balance
                            LedgerColumnBox(
                                label = "Closing",
                                amount = closingBalance?.let { CurrencyFormatter.formatAmountOnly(it) } ?: "—",
                                color = HermoditColors.TextPrimary,
                                arrow = "◷",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Filter Chips Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterPill(
                                    label = "All (${transactions.size})",
                                    isSelected = selectedFilter == null,
                                    onClick = { selectedFilter = null }
                                )
                            }
                            item {
                                val receivedCount = transactions.count { it.type == TransactionType.RECEIVED }
                                FilterPill(
                                    label = "Received ($receivedCount)",
                                    isSelected = selectedFilter == TransactionType.RECEIVED,
                                    onClick = { selectedFilter = TransactionType.RECEIVED }
                                )
                            }
                            item {
                                val withdrawnCount = transactions.count { it.type == TransactionType.WITHDRAWAL }
                                FilterPill(
                                    label = "Withdrawn ($withdrawnCount)",
                                    isSelected = selectedFilter == TransactionType.WITHDRAWAL,
                                    onClick = { selectedFilter = TransactionType.WITHDRAWAL }
                                )
                            }
                        }
                    }
                }
            }

            // Section Header: Chronological Feed
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PARSED SMS ENTRIES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HermoditColors.TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
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
                            text = "LOCAL SYNC OK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = HermoditColors.Primary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Transaction Cards List
            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions found for the selected filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HermoditColors.TextSecondary
                        )
                    }
                }
            } else {
                items(
                    items = filteredTransactions,
                    key = { it.id }
                ) { tx ->
                    TransactionRowCard(
                        transaction = tx,
                        onClick = { onNavigateToTransactionDetail(tx.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LedgerColumnBox(
    label: String,
    amount: String,
    color: Color,
    arrow: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, HermoditColors.OutlineVariant, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = HermoditColors.TextSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = arrow,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = color,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                text = "ETB",
                style = MaterialTheme.typography.labelSmall,
                color = HermoditColors.TextMuted,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) Color(0xFFE0F2FE) else HermoditColors.SurfaceCard
    val border = if (isSelected) Color(0xFFBAE6FD) else HermoditColors.OutlineVariant
    val color = if (isSelected) HermoditColors.Primary else HermoditColors.TextSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = color,
            fontSize = 12.sp
        )
    }
}

private fun formatHeaderDate(date: LocalDate): String {
    return "${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}, ${date.year}"
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DailyDetailScreenPreview() {
    com.example.hermodit.ui.theme.HermoditTheme {
        DailyDetailScreen(
            date = LocalDate.of(2026, 11, 16),
            repository = com.example.hermodit.data.repository.TransactionRepositoryImpl(
                com.example.hermodit.data.datasource.InMemoryTransactionDataSource()
            ),
            onNavigateBack = {},
            onNavigateToTransactionDetail = {}
        )
    }
}

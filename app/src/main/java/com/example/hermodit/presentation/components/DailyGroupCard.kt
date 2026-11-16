package com.example.hermodit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hermodit.R
import com.example.hermodit.core.util.CurrencyFormatter
import com.example.hermodit.domain.model.DailyTransactionGroup
import com.example.hermodit.presentation.theme.HermoditColors
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Daily Transaction Group Card strictly matching Stitch main_dashboard specs.
 */
@Composable
fun DailyGroupCard(
    group: DailyTransactionGroup,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isToday: Boolean = false,
) {
    val summary = group.summary
    val borderColor = if (isToday) HermoditColors.PrimaryBorder else HermoditColors.OutlineVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HermoditColors.SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Date & Transaction count pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(HermoditColors.Primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = formatGroupDate(summary.date, isToday),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HermoditColors.TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isToday) HermoditColors.PrimaryLight else HermoditColors.SurfaceNested)
                        .border(
                            1.dp,
                            if (isToday) HermoditColors.PrimaryBorder else HermoditColors.OutlineVariant,
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${summary.transactionCount} Transactions",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isToday) HermoditColors.Primary else HermoditColors.TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2-Column Metrics Grid: Received vs Withdrawn
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Inflow: Received & Deposited
                val totalIn = summary.totalReceived.add(summary.totalDeposited)
                MetricBox(
                    label = "RECEIVED",
                    amount = "+${CurrencyFormatter.formatAmountOnly(totalIn)} ETB",
                    isPositive = true,
                    iconRes = R.drawable.ic_arrow_downward,
                    modifier = Modifier.weight(1f)
                )

                // Outflow: Withdrawn
                MetricBox(
                    label = "WITHDRAWN",
                    amount = "-${CurrencyFormatter.formatAmountOnly(summary.totalWithdrawn)} ETB",
                    isPositive = false,
                    iconRes = R.drawable.ic_arrow_upward,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Net movement, EOD balance & Chevron tap cue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Net movement pill
                    val isNetPositive = summary.netMovement >= BigDecimal.ZERO
                    val netBg = if (isNetPositive) HermoditColors.InflowEmeraldContainer else HermoditColors.OutflowCrimsonContainer
                    val netBorder = if (isNetPositive) HermoditColors.InflowEmeraldBorder else HermoditColors.OutflowCrimsonBorder
                    val netColor = if (isNetPositive) HermoditColors.InflowEmerald else HermoditColors.OutflowCrimson

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(netBg)
                            .border(1.dp, netBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${if (isNetPositive) "+" else ""}${CurrencyFormatter.formatAmountOnly(summary.netMovement)} ETB",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = netColor,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    summary.latestReportedBalance?.let { eod ->
                        Text(
                            text = "EOD: ${CurrencyFormatter.formatAmountOnly(eod)} ETB",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = HermoditColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Tap cue
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isToday) "View ${summary.transactionCount} txs" else "Inspect",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isToday) HermoditColors.Primary else HermoditColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isToday) FontWeight.Medium else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = "Inspect",
                        tint = if (isToday) HermoditColors.Primary else HermoditColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    amount: String,
    isPositive: Boolean,
    @androidx.annotation.DrawableRes iconRes: Int,
    modifier: Modifier = Modifier
) {
    val bg = if (isPositive) HermoditColors.InflowEmeraldContainer else HermoditColors.OutflowCrimsonContainer
    val border = if (isPositive) HermoditColors.InflowEmeraldBorder else HermoditColors.OutflowCrimsonBorder
    val textColor = if (isPositive) HermoditColors.InflowEmerald else HermoditColors.OutflowCrimson

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .padding(10.dp)
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
                    fontWeight = FontWeight.Bold,
                    color = HermoditColors.TextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = textColor,
                fontSize = 13.sp
            )
        }
    }
}

private fun formatGroupDate(date: LocalDate, isToday: Boolean): String {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    return when {
        isToday || date == today -> "Today, ${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}, ${date.year}"
        date == yesterday -> "Yesterday, ${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}, ${date.year}"
        else -> "${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}, ${date.year}"
    }
}

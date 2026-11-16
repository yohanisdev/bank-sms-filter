package com.example.hermodit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hermodit.core.util.CurrencyFormatter
import com.example.hermodit.core.util.DateTimeUtils
import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.model.TransactionType
import com.example.hermodit.presentation.theme.HermoditColors

/**
 * Transaction Row Card matching Stitch Daily Transaction Details list view.
 */
@Composable
fun TransactionRowCard(
    transaction: FinancialTransaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInflow = transaction.type == TransactionType.RECEIVED || transaction.type == TransactionType.DEPOSIT
    val badgeBg = if (isInflow) HermoditColors.InflowEmeraldContainer else HermoditColors.OutflowCrimsonContainer
    val badgeBorder = if (isInflow) HermoditColors.InflowEmeraldBorder else HermoditColors.OutflowCrimsonBorder
    val amountColor = if (isInflow) HermoditColors.InflowEmerald else HermoditColors.OutflowCrimson
    val arrowText = if (isInflow) "↓" else "↑"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = HermoditColors.SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, HermoditColors.OutlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Direction circle badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeBg)
                    .border(1.dp, badgeBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = arrowText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction title, time, and reported balance
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.counterpartyName ?: "${transaction.bankName} ${transaction.type}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HermoditColors.TextPrimary,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val timeStr = transaction.transactionTime?.let {
                        val hour = if (it.hour % 12 == 0) 12 else it.hour % 12
                        val amPm = if (it.hour < 12) "AM" else "PM"
                        String.format(java.util.Locale.US, "%02d:%02d %s", hour, it.minute, amPm)
                    } ?: DateTimeUtils.formatTime(transaction.smsTimestamp)

                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = HermoditColors.TextSecondary,
                        fontSize = 12.sp
                    )

                    transaction.availableBalance?.let { bal ->
                        Text(
                            text = " • ",
                            color = HermoditColors.Outline,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Avail: ${CurrencyFormatter.formatAmountOnly(bal)} ETB",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = HermoditColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount and chevron
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isInflow) "+" else "-"}${CurrencyFormatter.formatAmountOnly(transaction.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = amountColor
                )
                Text(
                    text = transaction.currency,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "›",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HermoditColors.Outline
            )
        }
    }
}

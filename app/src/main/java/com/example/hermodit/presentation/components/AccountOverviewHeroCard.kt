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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hermodit.core.util.CurrencyFormatter
import com.example.hermodit.domain.model.BankAccountConfig
import com.example.hermodit.presentation.theme.HermoditColors
import java.math.BigDecimal

/**
 * Account Overview Hero Card matching the Stitch Main Dashboard specifications.
 */
@Composable
fun AccountOverviewHeroCard(
    accountConfig: BankAccountConfig,
    availableBalance: BigDecimal?,
    onSyncNowClick: () -> Unit,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSyncing: Boolean = false,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HermoditColors.SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, HermoditColors.OutlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top Row: Bank Info & Masked Account & Savings pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onAccountClick)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF0F9FF))
                                .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = accountConfig.bankCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HermoditColors.Primary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = accountConfig.bankName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = HermoditColors.TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = accountConfig.accountIdentifier,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = HermoditColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "|",
                            color = HermoditColors.Outline,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SMS Shield Active",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = HermoditColors.InflowEmeraldDark,
                            fontSize = 11.sp
                        )
                    }
                }

                // Account type pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(HermoditColors.InflowEmeraldContainer)
                        .border(1.dp, HermoditColors.InflowEmeraldBorder, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(HermoditColors.InflowEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = accountConfig.accountType,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = HermoditColors.InflowEmeraldDark,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Center Display: Available Aggregate Balance
            Text(
                text = "AVAILABLE AGGREGATE BALANCE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = HermoditColors.TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (availableBalance != null) {
                        CurrencyFormatter.formatAmountOnly(availableBalance)
                    } else {
                        "—"
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 34.sp,
                    color = HermoditColors.TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ETB",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HermoditColors.Primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer: Last SMS sync & Sync Now action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF1F5F9))
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Last SMS sync: Today, 10:42 AM",
                        style = MaterialTheme.typography.bodySmall,
                        color = HermoditColors.TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "1 configured account",
                        style = MaterialTheme.typography.labelSmall,
                        color = HermoditColors.TextMuted,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onSyncNowClick,
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(containerColor = HermoditColors.Primary),
                    shape = RoundedCornerShape(50),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (isSyncing) "Syncing..." else "Sync Now",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

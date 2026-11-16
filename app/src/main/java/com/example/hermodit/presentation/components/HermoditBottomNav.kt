package com.example.hermodit.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hermodit.R
import com.example.hermodit.presentation.navigation.Screen
import com.example.hermodit.presentation.theme.HermoditColors

/**
 * Bottom Navigation Bar matching Stitch design with Dashboard, Transactions, and Settings destinations.
 */
@Composable
fun HermoditBottomNav(
    currentScreen: Screen,
    onDashboardClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = HermoditColors.SurfaceCard,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, HermoditColors.OutlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isDashboardActive = currentScreen is Screen.Dashboard
            val isTransactionsActive = currentScreen is Screen.DailyDetail || currentScreen is Screen.TransactionDetail
            val isSettingsActive = currentScreen is Screen.Settings || currentScreen is Screen.AccountConfig

            // Dashboard item
            NavItem(
                iconRes = R.drawable.ic_dashboard,
                label = "Dashboard",
                isActive = isDashboardActive,
                onClick = onDashboardClick
            )

            // Transactions item
            NavItem(
                iconRes = R.drawable.ic_receipt_long,
                label = "Transactions",
                isActive = isTransactionsActive,
                onClick = onTransactionsClick
            )

            // Settings item
            NavItem(
                iconRes = R.drawable.ic_settings,
                label = "Settings",
                isActive = isSettingsActive,
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun NavItem(
    @DrawableRes iconRes: Int,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .then(
                if (isActive) {
                    Modifier
                        .background(HermoditColors.PrimaryLight)
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                } else {
                    Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = if (isActive) HermoditColors.Primary else HermoditColors.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isActive) HermoditColors.Primary else HermoditColors.TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

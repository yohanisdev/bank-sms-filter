package com.example.hermodit.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.hermodit.domain.model.FinancialTransaction

/**
 * Reusable list item card representing a single financial transaction.
 * Delegates to the Stitch-styled TransactionRowCard.
 */
@Composable
fun TransactionItemCard(
    transaction: FinancialTransaction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    TransactionRowCard(
        transaction = transaction,
        onClick = onClick,
        modifier = modifier
    )
}

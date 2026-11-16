package com.example.hermodit.core.util

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Utility for formatting financial currency values.
 */
object CurrencyFormatter {
    fun format(
        amount: BigDecimal,
        currencyCode: String = "ETB",
        locale: Locale = Locale.US
    ): String {
        return if (currencyCode.equals("ETB", ignoreCase = true)) {
            val symbols = DecimalFormatSymbols(locale)
            val formatter = DecimalFormat("#,##0.00", symbols)
            "${formatter.format(amount)} ETB"
        } else {
            val format = NumberFormat.getCurrencyInstance(locale).apply {
                try {
                    currency = Currency.getInstance(currencyCode)
                } catch (_: IllegalArgumentException) {
                    currency = Currency.getInstance("USD")
                }
            }
            format.format(amount)
        }
    }

    fun formatAmountOnly(
        amount: BigDecimal,
        locale: Locale = Locale.US
    ): String {
        val symbols = DecimalFormatSymbols(locale)
        val formatter = DecimalFormat("#,##0.00", symbols)
        return formatter.format(amount)
    }

    fun formatWithSign(
        amount: BigDecimal,
        isCredit: Boolean,
        currencyCode: String = "ETB",
        locale: Locale = Locale.US
    ): String {
        val formatted = format(amount.abs(), currencyCode, locale)
        return if (isCredit) "+$formatted" else "-$formatted"
    }
}

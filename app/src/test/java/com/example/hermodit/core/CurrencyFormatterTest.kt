package com.example.hermodit.core

import com.example.hermodit.core.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class CurrencyFormatterTest {

    @Test
    fun format_usdAmount_formatsCorrectly() {
        val amount = BigDecimal("1234.56")
        val formatted = CurrencyFormatter.format(amount, "USD", Locale.US)
        assertEquals("$1,234.56", formatted)
    }

    @Test
    fun formatWithSign_credit_hasPlusPrefix() {
        val amount = BigDecimal("500.00")
        val formatted = CurrencyFormatter.formatWithSign(amount, isCredit = true, "USD", Locale.US)
        assertTrue(formatted.startsWith("+$"))
        assertEquals("+$500.00", formatted)
    }

    @Test
    fun formatWithSign_debit_hasMinusPrefix() {
        val amount = BigDecimal("45.20")
        val formatted = CurrencyFormatter.formatWithSign(amount, isCredit = false, "USD", Locale.US)
        assertTrue(formatted.startsWith("-$"))
        assertEquals("-$45.20", formatted)
    }
}

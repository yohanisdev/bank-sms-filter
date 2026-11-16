package com.example.hermodit.data.sms

import com.example.hermodit.domain.model.BankAccountConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AccountMatcherTest {

    private lateinit var config: BankAccountConfig
    private lateinit var matcher: AccountMatcher

    @Before
    fun setUp() {
        config = BankAccountConfig(
            bankName = "Commercial Bank of Ethiopia",
            bankCode = "CBE",
            accountIdentifier = "•••• 3608",
            fullAccountNumber = "1000763003608",
            accountHolder = "YOANNIS KEFYALEW AND MEKLIT ASHENAF",
            accountType = "SAVINGS"
        )
        matcher = AccountMatcher(config)
    }

    @Test
    fun match_exactFullNumber_returnsMatched() {
        val result = matcher.match(
            extractedAccount = "1000763003608",
            smsBody = "Deposit to account 1000763003608 completed."
        )
        assertTrue(result is AccountMatchResult.Matched)
        assertEquals("•••• 3608", (result as AccountMatchResult.Matched).accountIdentifier)
    }

    @Test
    fun match_maskedEndingWith3608_returnsMatched() {
        val result = matcher.match(
            extractedAccount = "3608",
            smsBody = "Deposit to A/C ••••3608 confirmed."
        )
        assertTrue(result is AccountMatchResult.Matched)
        assertEquals("•••• 3608", (result as AccountMatchResult.Matched).accountIdentifier)
    }

    @Test
    fun match_prefixMatchesFullAccountAndEndsWith3608_returnsMatched() {
        val result = matcher.match(
            extractedAccount = "10003608",
            smsBody = "Deposit to A/C 1000...3608 confirmed."
        )
        assertTrue(result is AccountMatchResult.Matched)
    }

    @Test
    fun match_differentAccount_returnsNotMatched() {
        val result = matcher.match(
            extractedAccount = "1000123456789",
            smsBody = "Transfer to account 1000123456789 confirmed."
        )
        assertTrue(result is AccountMatchResult.NotMatched)
    }

    @Test
    fun match_differentMaskedAccount_returnsNotMatched() {
        val result = matcher.match(
            extractedAccount = "4582",
            smsBody = "Transfer to A/C ••••4582 confirmed."
        )
        assertTrue(result is AccountMatchResult.NotMatched)
    }

    @Test
    fun match_accountHolderNamePresentWithoutAccountNumber_returnsAmbiguous() {
        val result = matcher.match(
            extractedAccount = null,
            smsBody = "Payment received for YOANNIS KEFYALEW. Thank you."
        )
        assertTrue(result is AccountMatchResult.Ambiguous)
    }

    @Test
    fun match_neitherAccountNorName_returnsNotMatched() {
        val result = matcher.match(
            extractedAccount = null,
            smsBody = "Your OTP is 123456. Do not share it."
        )
        assertTrue(result is AccountMatchResult.NotMatched)
    }
}

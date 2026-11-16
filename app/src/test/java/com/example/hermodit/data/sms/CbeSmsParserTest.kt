package com.example.hermodit.data.sms

import com.example.hermodit.data.datasource.InMemoryTransactionDataSource
import com.example.hermodit.data.repository.TransactionRepositoryImpl
import com.example.hermodit.domain.model.BankAccountConfig
import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.model.TransactionType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/**
 * Deterministic unit tests for CBE SMS parser covering real-world patterns A, B, C, D
 * and boundary conditions.
 *
 * NOTE ON TEST FIXTURES:
 * The 4 example structures provided (Pattern A, B, C, D) are structural references derived from
 * partially redacted/masked production messages. Tests verify deterministic parsing and strict
 * handling of redacted/missing values. Full verification on actual live telecom payloads requires
 * exact unredacted SMS captures with timestamps and gateway reference tokens.
 */
class CbeSmsParserTest {

    private lateinit var parser: CbeSmsParser
    private lateinit var config: BankAccountConfig

    @Before
    fun setUp() {
        parser = CbeSmsParser()
        config = BankAccountConfig(
            bankName = "Commercial Bank of Ethiopia",
            bankCode = "CBE",
            accountIdentifier = "•••• 3608",
            fullAccountNumber = "1000763003608",
            accountHolder = "YOANNIS KEFYALEW AND MEKLIT ASHENAF",
            accountType = "SAVINGS"
        )
    }

    /**
     * Pattern A: Incoming Telebirr-to-CBE credit
     * "Dear Yohannis Kefiyale your Account ...3608 has been credited with ETB 10. Your Current Balance is ETB 4243.1."
     */
    @Test
    fun parse_patternA_incomingTelebirrCredit_success() {
        val sms = "Dear Yohannis Kefiyale your Account ...3608 has been credited with ETB 10. Your Current Balance is ETB 4243.1."
        val result = parser.parse(
            smsId = "tb-01",
            senderAddress = "telebirr",
            messageBody = sms,
            smsTimestamp = Instant.parse("2026-11-16T08:30:00Z"),
            config = config
        )

        assertTrue("Expected Success but got $result", result is ParseResult.Success)
        val tx = (result as ParseResult.Success).transaction
        assertEquals(TransactionType.RECEIVED, tx.type)
        assertEquals(BigDecimal("10.00"), tx.amount)
        assertEquals("ETB", tx.currency)
        assertEquals("•••• 3608", tx.accountIdentifier)
        // Per requirement: display name "Unknown sender" while underlying rawSenderName is null
        assertEquals("Unknown sender", tx.counterpartyName)
        assertNull(tx.rawSenderName)
        assertNull(tx.counterpartyAccount)
        assertEquals(BigDecimal("4243.10"), tx.availableBalance)
        assertEquals("TELEBIRR", tx.channel)
    }

    /**
     * Pattern B: Incoming CBE-to-CBE transfer
     * "You have received ETB [amount] from account [sender account] ([sender name]) to your account [recipient account]. Your current balance is ETB [balance]."
     */
    @Test
    fun parse_patternB_incomingCbeTransfer_success() {
        val sms = "You have received ETB 1,500.00 from account ...9876 (Abebe Bikila) to your account ...3608. Your current balance is ETB 12,500.50."
        val result = parser.parse(
            smsId = "cbe-in-01",
            senderAddress = "CBE",
            messageBody = sms,
            smsTimestamp = Instant.parse("2026-11-16T09:15:00Z"),
            config = config
        )

        assertTrue("Expected Success but got $result", result is ParseResult.Success)
        val tx = (result as ParseResult.Success).transaction
        assertEquals(TransactionType.RECEIVED, tx.type)
        assertEquals(BigDecimal("1500.00"), tx.amount)
        assertEquals("ETB", tx.currency)
        assertEquals("•••• 3608", tx.accountIdentifier)
        assertEquals("...9876", tx.counterpartyAccount)
        assertEquals("Abebe Bikila", tx.counterpartyName)
        assertEquals("Abebe Bikila", tx.rawSenderName)
        assertEquals(BigDecimal("12500.50"), tx.availableBalance)
        assertEquals("TRANSFER", tx.channel)
    }

    /**
     * Pattern C: Outgoing CBE transfer
     * "You have successfully transferred ETB 310.00 from account ...3608 to account ...1626 (Gutema Mitiku Gemeda). Service charge of ETB 0.50 and VAT(15%) of ETB 0.08 and Disaster Recovery(5%) of 0.03 with total of ETB 310.61."
     */
    @Test
    fun parse_patternC_outgoingCbeTransfer_success() {
        val sms = "You have successfully transferred ETB 310.00 from account ...3608 to account ...1626 (Gutema Mitiku Gemeda). Service charge of ETB 0.50 and VAT(15%) of ETB 0.08 and Disaster Recovery(5%) of 0.03 with total of ETB 310.61."
        val result = parser.parse(
            smsId = "cbe-out-01",
            senderAddress = "CBE",
            messageBody = sms,
            smsTimestamp = Instant.parse("2026-11-16T10:00:00Z"),
            config = config
        )

        assertTrue("Expected Success but got $result", result is ParseResult.Success)
        val tx = (result as ParseResult.Success).transaction
        assertEquals(TransactionType.WITHDRAWAL, tx.type)
        assertEquals(BigDecimal("310.00"), tx.amount)
        assertEquals("ETB", tx.currency)
        assertEquals("•••• 3608", tx.accountIdentifier)
        assertEquals("...1626", tx.counterpartyAccount)
        assertEquals("Gutema Mitiku Gemeda", tx.counterpartyName)
        // Fees kept strictly separate from the transfer amount
        assertEquals(BigDecimal("0.50"), tx.serviceCharge)
        assertEquals(BigDecimal("0.08"), tx.vat)
        assertEquals(BigDecimal("0.03"), tx.disasterRecoveryCharge)
        assertEquals(BigDecimal("310.61"), tx.totalDebit)
    }

    /**
     * Pattern D: CBE cash withdrawal
     * "Dear Yohannis Kefiyale your Account ...3608 has been debited with ETB 7000. Your Current Balance is ETB 1438.1."
     */
    @Test
    fun parse_patternD_cashWithdrawal_success() {
        val sms = "Dear Yohannis Kefiyale your Account ...3608 has been debited with ETB 7000. Your Current Balance is ETB 1438.1."
        val result = parser.parse(
            smsId = "cbe-debit-01",
            senderAddress = "CBE",
            messageBody = sms,
            smsTimestamp = Instant.parse("2026-11-16T11:00:00Z"),
            config = config
        )

        assertTrue("Expected Success but got $result", result is ParseResult.Success)
        val tx = (result as ParseResult.Success).transaction
        assertEquals(TransactionType.WITHDRAWAL, tx.type)
        assertEquals(BigDecimal("7000.00"), tx.amount)
        assertEquals("ETB", tx.currency)
        assertEquals("•••• 3608", tx.accountIdentifier)
        assertEquals(BigDecimal("1438.10"), tx.availableBalance)
        assertNull("Channel should be null unless reliable evidence identifies ATM/POS", tx.channel)
        assertNull(tx.counterpartyName)
    }

    /**
     * Missing sender information in incoming CBE transfer
     * Display name defaults to "Unknown sender" while rawSenderName is null
     */
    @Test
    fun parse_missingSenderInformation_handledGracefully() {
        val sms = "You have received ETB 200.00 from account ...9876 to your account ...3608. Your current balance is ETB 12,700.50."
        val result = parser.parse(
            smsId = "cbe-in-02",
            senderAddress = "CBE",
            messageBody = sms,
            smsTimestamp = Instant.now(),
            config = config
        )

        assertTrue(result is ParseResult.Success)
        val tx = (result as ParseResult.Success).transaction
        assertEquals("Unknown sender", tx.counterpartyName)
        assertNull(tx.rawSenderName)
        assertEquals("...9876", tx.counterpartyAccount)
        assertEquals(BigDecimal("200.00"), tx.amount)
    }

    /**
     * Missing balance in incoming CBE transfer
     * availableBalance should be null (never invented or calculated)
     */
    @Test
    fun parse_missingBalance_balanceStoredAsNull() {
        val sms = "You have received ETB 300.00 from account ...9876 (Abebe Bikila) to your account ...3608."
        val result = parser.parse(
            smsId = "cbe-in-03",
            senderAddress = "CBE",
            messageBody = sms,
            smsTimestamp = Instant.now(),
            config = config
        )

        assertTrue(result is ParseResult.Success)
        val tx = (result as ParseResult.Success).transaction
        assertNull("Missing balance must be stored as null", tx.availableBalance)
        assertEquals(BigDecimal("300.00"), tx.amount)
    }

    /**
     * Masked account matching supports both masked ...3608 and full 1000763003608
     */
    @Test
    fun parse_maskedAccountMatching_exactAndMaskedBothMatch() {
        val fullSms = "Dear Yohannis Kefiyale your Account 1000763003608 has been credited with ETB 50. Your Current Balance is ETB 4293.1."
        val fullResult = parser.parse("cbe-full", "CBE", fullSms, Instant.now(), config)
        assertTrue(fullResult is ParseResult.Success)

        val maskedSms = "Dear Yohannis Kefiyale your Account ••••3608 has been credited with ETB 50. Your Current Balance is ETB 4293.1."
        val maskedResult = parser.parse("cbe-masked", "CBE", maskedSms, Instant.now(), config)
        assertTrue(maskedResult is ParseResult.Success)
    }

    /**
     * Wrong recipient account: incoming transfer where recipient is another account (...9999)
     * Even if sender account is ...3608, recipient account must match monitored account!
     */
    @Test
    fun parse_wrongRecipientAccount_rejectedWithAccountMismatch() {
        val sms = "You have received ETB 500.00 from account ...3608 (Yohannis) to your account ...9999. Your current balance is ETB 1,000.00."
        val result = parser.parse(
            smsId = "cbe-wrong-recip",
            senderAddress = "CBE",
            messageBody = sms,
            smsTimestamp = Instant.now(),
            config = config
        )

        assertTrue("Transfer to another account must be rejected", result is ParseResult.AccountMismatch)
        val mismatch = result as ParseResult.AccountMismatch
        assertEquals("...9999", mismatch.extractedAccount)
    }

    /**
     * Wrong source account: outgoing transfer where source is another account (...9999)
     * Even if recipient is ...3608, source account must match monitored account!
     */
    @Test
    fun parse_wrongSourceAccount_outgoingTransfer_rejectedWithAccountMismatch() {
        val sms = "You have successfully transferred ETB 100.00 from account ...9999 to account ...3608 (Yohannis K)."
        val result = parser.parse(
            smsId = "cbe-wrong-src",
            senderAddress = "CBE",
            messageBody = sms,
            smsTimestamp = Instant.now(),
            config = config
        )

        assertTrue("Outgoing transfer from another account must be rejected", result is ParseResult.AccountMismatch)
        val mismatch = result as ParseResult.AccountMismatch
        assertEquals("...9999", mismatch.extractedAccount)
    }

    /**
     * Unrecognized message: non-financial notification or OTP
     */
    @Test
    fun parse_unrecognizedMessage_returnsIgnored() {
        val sms = "Your one-time verification code is 482910. Do not share this with anyone."
        val result = parser.parse(
            smsId = "otp-01",
            senderAddress = "CBE",
            messageBody = sms,
            smsTimestamp = Instant.now(),
            config = config
        )

        assertTrue("Non-transaction message must be ignored", result is ParseResult.Ignored)
    }

    /**
     * Duplicate SMS detection via repository and SmsDeduplicator
     */
    @Test
    fun parse_duplicateSms_detectedByDeduplicator() = runBlocking {
        val dataSource = InMemoryTransactionDataSource()
        val repository = TransactionRepositoryImpl(dataSource)
        val deduplicator = SmsDeduplicator(repository)

        val tx = FinancialTransaction(
            id = "tx-existing",
            bankName = "Commercial Bank of Ethiopia",
            accountIdentifier = "•••• 3608",
            type = TransactionType.RECEIVED,
            amount = BigDecimal("100.00"),
            counterpartyName = "Deposit Cash",
            transactionDate = LocalDate.now(),
            sourceIdentifier = "sms-dup-01",
            bankReferenceNumber = "FT998877",
            smsTimestamp = Instant.now()
        )
        repository.addTransaction(tx)

        // Verifying deduplication against source ID and reference number
        assertTrue("Must detect duplicate by source identifier", deduplicator.isDuplicate("sms-dup-01", null))
        assertTrue("Must detect duplicate by bank reference number", deduplicator.isDuplicate("sms-new-id", "FT998877"))
        assertFalse("New unique SMS must not be flagged as duplicate", deduplicator.isDuplicate("sms-unique", "FT_UNIQUE"))
    }

    /**
     * Obscured or malformed amounts: redacted placeholders or invalid numbers
     * Must return InvalidFormat and never fabricate an amount
     */
    @Test
    fun parse_obscuredOrMalformedAmount_returnsInvalidFormat() {
        val smsWithRedactedAmount = "You have received ETB [amount] from account ...9876 to your account ...3608."
        val result = parser.parse(
            smsId = "cbe-malformed-01",
            senderAddress = "CBE",
            messageBody = smsWithRedactedAmount,
            smsTimestamp = Instant.now(),
            config = config
        )

        assertTrue("Malformed amount must return InvalidFormat", result is ParseResult.InvalidFormat)
    }

    /**
     * Ambiguous account match: message mentions account holder name but lacks account number
     */
    @Test
    fun parse_holderNameWithoutAccountNumber_flaggedAsAmbiguous() {
        val sms = "Payment of ETB 500.00 confirmed for YOANNIS KEFYALEW. Ref: AMB123."
        val result = parser.parse("sms-amb", "CBE", sms, Instant.now(), config)

        assertTrue(result is ParseResult.Ambiguous)
        val amb = result as ParseResult.Ambiguous
        assertTrue(amb.record.reason.contains("account holder name"))
    }
}

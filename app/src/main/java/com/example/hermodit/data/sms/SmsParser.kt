package com.example.hermodit.data.sms

import com.example.hermodit.domain.model.BankAccountConfig
import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.model.UnmatchedSmsRecord
import java.time.Instant

sealed interface ParseResult {
    data class Success(val transaction: FinancialTransaction) : ParseResult
    data class Ambiguous(val record: UnmatchedSmsRecord) : ParseResult
    data class AccountMismatch(val extractedAccount: String?, val reason: String) : ParseResult
    data class Ignored(val reason: String) : ParseResult
    data class InvalidFormat(val reason: String) : ParseResult
}

/**
 * Bank-specific parser interface for SMS transaction extraction.
 */
interface SmsParser {
    val bankCode: String

    fun canHandle(senderAddress: String, messageBody: String): Boolean

    fun parse(
        smsId: String,
        senderAddress: String,
        messageBody: String,
        smsTimestamp: Instant,
        config: BankAccountConfig
    ): ParseResult
}

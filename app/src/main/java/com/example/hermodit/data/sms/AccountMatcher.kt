package com.example.hermodit.data.sms

import com.example.hermodit.domain.model.BankAccountConfig

sealed interface AccountMatchResult {
    data class Matched(val accountIdentifier: String) : AccountMatchResult
    data class Ambiguous(val reason: String) : AccountMatchResult
    data class NotMatched(val reason: String) : AccountMatchResult
}

/**
 * Strict account matcher for the single monitored CBE bank account.
 *
 * Rules:
 * - Read monitored account from [BankAccountConfig] (no hardcoded numbers).
 * - Match full account numbers and reliable bank-masked numbers.
 * - Verify recipient account for incoming transfers.
 * - Verify source account for outgoing transfers.
 * - Never accept a transaction solely because 3608 appears somewhere in text.
 * - Do not match using account-holder name alone.
 * - Messages with holder name but missing verified account number are returned as [AccountMatchResult.Ambiguous].
 */
class AccountMatcher(
    private val config: BankAccountConfig
) {
    private val normalizedFullNumber = normalizeAccountNumber(config.fullAccountNumber)
    private val last4Digits = if (normalizedFullNumber.length >= 4) {
        normalizedFullNumber.takeLast(4)
    } else {
        ""
    }

    /**
     * Verifies that the appropriate party account for the message type belongs to the monitored account.
     */
    fun verifyPartyAccount(
        messageType: CbeMessageType,
        sourceAccount: String?,
        recipientAccount: String?,
        generalAccount: String?,
        smsBody: String
    ): AccountMatchResult {
        return when (messageType) {
            CbeMessageType.OUTGOING_TRANSFER -> {
                // Must verify the SOURCE account (the account being debited)
                if (sourceAccount.isNullOrBlank()) {
                    match(null, smsBody)
                } else {
                    val res = match(sourceAccount, smsBody)
                    if (res is AccountMatchResult.NotMatched) {
                        AccountMatchResult.NotMatched("Outgoing transfer source account ($sourceAccount) does not match monitored account")
                    } else {
                        res
                    }
                }
            }
            CbeMessageType.INCOMING_TRANSFER -> {
                // Must verify the RECIPIENT account (the account receiving funds)
                if (recipientAccount.isNullOrBlank()) {
                    match(null, smsBody)
                } else {
                    val res = match(recipientAccount, smsBody)
                    if (res is AccountMatchResult.NotMatched) {
                        AccountMatchResult.NotMatched("Incoming transfer recipient account ($recipientAccount) does not match monitored account")
                    } else {
                        res
                    }
                }
            }
            CbeMessageType.CREDIT_RECEIPT, CbeMessageType.BRANCH_DEPOSIT -> {
                val target = recipientAccount ?: generalAccount
                match(target, smsBody)
            }
            CbeMessageType.DEBIT_OR_WITHDRAWAL -> {
                val target = sourceAccount ?: generalAccount
                match(target, smsBody)
            }
            CbeMessageType.UNKNOWN -> {
                match(generalAccount, smsBody)
            }
        }
    }

    fun match(extractedAccount: String?, smsBody: String): AccountMatchResult {
        if (!extractedAccount.isNullOrBlank()) {
            val normalizedExtracted = normalizeAccountNumber(extractedAccount)

            // Case 1: Exact full account match
            if (normalizedExtracted.length == normalizedFullNumber.length && normalizedExtracted == normalizedFullNumber) {
                return AccountMatchResult.Matched(config.accountIdentifier)
            }

            // Case 2: Masked account match (e.g., ...3608, ••••3608, *3608, 1000...3608)
            // The unmasked tail must match the last 4 digits of the monitored account
            if (normalizedExtracted.length >= 4 && normalizedExtracted.endsWith(last4Digits)) {
                // If there is a prefix in the extracted number, it must match the start of the full number
                if (normalizedExtracted.length > 4) {
                    val prefix = normalizedExtracted.dropLast(4)
                    if (normalizedFullNumber.startsWith(prefix)) {
                        return AccountMatchResult.Matched(config.accountIdentifier)
                    } else {
                        return AccountMatchResult.NotMatched("Account prefix mismatch ($prefix does not match monitored account prefix)")
                    }
                }
                return AccountMatchResult.Matched(config.accountIdentifier)
            }

            // Case 3: Extracted account does not match
            return AccountMatchResult.NotMatched(
                "Extracted account '$extractedAccount' (normalized: $normalizedExtracted) does not match monitored account tail $last4Digits"
            )
        }

        // Account number is missing from the message.
        // Check if account holder name is mentioned.
        val holderTokens = config.accountHolder.split("\\s+".toRegex()).filter { it.length > 3 }
        val matchesHolderName = holderTokens.isNotEmpty() && holderTokens.any { smsBody.contains(it, ignoreCase = true) }

        if (matchesHolderName) {
            // Requirement: "Support the account holder name as additional matching evidence, but do not treat a name alone as proof of account ownership.
            // Record ambiguous messages separately as unmatched or requiring review."
            return AccountMatchResult.Ambiguous(
                "Message mentions account holder name but lacks account number verification"
            )
        }

        return AccountMatchResult.NotMatched("No account number found in message")
    }

    companion object {
        fun normalizeAccountNumber(raw: String): String {
            return raw.filter { it.isDigit() }
        }
    }
}

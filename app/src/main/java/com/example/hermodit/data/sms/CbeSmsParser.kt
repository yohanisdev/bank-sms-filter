package com.example.hermodit.data.sms

import com.example.hermodit.domain.model.BankAccountConfig
import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.model.ParseStatus
import com.example.hermodit.domain.model.UnmatchedSmsRecord
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Deterministic, privacy-conscious parser for Commercial Bank of Ethiopia (CBE)
 * and Telebirr-to-CBE transaction SMS notifications.
 *
 * Implements deterministic parsing separated into:
 * 1. SMS Classification ([CbeMessageClassifier])
 * 2. Transaction Extraction ([CbeTransactionExtractor])
 * 3. Account Matching ([AccountMatcher])
 * 4. Validation & Structuring
 */
class CbeSmsParser : SmsParser {

    override val bankCode: String = "CBE"

    override fun canHandle(senderAddress: String, messageBody: String): Boolean {
        val normalizedSender = senderAddress.trim().uppercase()
        val isSupportedSender = normalizedSender in listOf("CBE", "CBE_BIRR", "889", "TELEBIRR") ||
            normalizedSender.contains("CBE") ||
            normalizedSender.contains("TELEBIRR")

        val messageType = CbeMessageClassifier.classify(messageBody)
        val looksLikeBankSms = messageType != CbeMessageType.UNKNOWN ||
            messageBody.contains("CBE", ignoreCase = true) ||
            messageBody.contains("ETB", ignoreCase = true) ||
            messageBody.contains("Birr", ignoreCase = true) ||
            messageBody.contains("Account", ignoreCase = true) ||
            messageBody.contains("A/C", ignoreCase = true)

        return isSupportedSender && looksLikeBankSms
    }

    override fun parse(
        smsId: String,
        senderAddress: String,
        messageBody: String,
        smsTimestamp: Instant,
        config: BankAccountConfig
    ): ParseResult {
        if (!canHandle(senderAddress, messageBody)) {
            return ParseResult.Ignored("Sender address or message body not recognized as supported CBE bank SMS")
        }

        // 1. Classification
        val messageType = CbeMessageClassifier.classify(messageBody)
        if (messageType == CbeMessageType.UNKNOWN) {
            val accountMatcher = AccountMatcher(config)
            val matchCheck = accountMatcher.match(null, messageBody)
            if (matchCheck is AccountMatchResult.Ambiguous) {
                return ParseResult.Ambiguous(
                    record = UnmatchedSmsRecord(
                        id = UUID.randomUUID().toString(),
                        senderAddress = senderAddress,
                        messageBody = messageBody,
                        receivedTimestamp = smsTimestamp,
                        reason = matchCheck.reason
                    )
                )
            }
            return ParseResult.Ignored("Message structure not recognized as a supported transaction pattern")
        }

        // 2. Extraction
        val extracted = CbeTransactionExtractor.extract(
            messageType = messageType,
            senderAddress = senderAddress,
            body = messageBody,
            smsTimestamp = smsTimestamp
        ) ?: return ParseResult.InvalidFormat("Unable to extract valid financial transaction fields from message")

        // 3. Account Matching (Strict party verification)
        val accountMatcher = AccountMatcher(config)
        val matchResult = accountMatcher.verifyPartyAccount(
            messageType = messageType,
            sourceAccount = extracted.sourceAccount,
            recipientAccount = extracted.recipientAccount,
            generalAccount = extracted.generalAccount,
            smsBody = messageBody
        )

        when (matchResult) {
            is AccountMatchResult.NotMatched -> {
                val partyAccount = when (messageType) {
                    CbeMessageType.OUTGOING_TRANSFER -> extracted.sourceAccount
                    CbeMessageType.INCOMING_TRANSFER -> extracted.recipientAccount
                    else -> extracted.recipientAccount ?: extracted.sourceAccount ?: extracted.generalAccount
                }
                return ParseResult.AccountMismatch(
                    extractedAccount = partyAccount,
                    reason = matchResult.reason
                )
            }

            is AccountMatchResult.Ambiguous -> {
                return ParseResult.Ambiguous(
                    record = UnmatchedSmsRecord(
                        id = UUID.randomUUID().toString(),
                        senderAddress = senderAddress,
                        messageBody = messageBody,
                        receivedTimestamp = smsTimestamp,
                        reason = matchResult.reason
                    )
                )
            }

            is AccountMatchResult.Matched -> {
                // Verified party account belongs to the monitored account
            }
        }

        // 4. Validation
        if (extracted.amount <= BigDecimal.ZERO) {
            return ParseResult.InvalidFormat("Transaction amount must be strictly positive")
        }
        if (extracted.currency != "ETB") {
            return ParseResult.InvalidFormat("Unsupported currency '${extracted.currency}' (expected ETB)")
        }

        // 5. Structure Transaction
        val transaction = FinancialTransaction(
            id = "tx-cbe-$smsId",
            bankName = config.bankName,
            accountIdentifier = matchResult.accountIdentifier,
            type = extracted.type,
            amount = extracted.amount,
            currency = extracted.currency,
            counterpartyName = extracted.counterpartyName,
            counterpartyAccount = extracted.counterpartyAccount,
            rawSenderName = extracted.rawSenderName,
            transactionDate = extracted.date,
            transactionTime = extracted.time,
            availableBalance = extracted.balance,
            bankReferenceNumber = extracted.reference,
            rawSmsContent = messageBody,
            sourceIdentifier = "sms-$smsId",
            smsTimestamp = smsTimestamp,
            smsSenderId = senderAddress,
            channel = extracted.channel,
            serviceCharge = extracted.serviceCharge,
            vat = extracted.vat,
            disasterRecoveryCharge = extracted.disasterRecoveryCharge,
            totalDebit = extracted.totalDebit,
            parseStatus = ParseStatus.CONFIRMED
        )

        return ParseResult.Success(transaction)
    }
}

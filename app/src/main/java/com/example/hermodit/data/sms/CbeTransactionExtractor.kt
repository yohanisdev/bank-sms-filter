package com.example.hermodit.data.sms

import com.example.hermodit.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ExtractedSmsData(
    val type: TransactionType,
    val amount: BigDecimal,
    val currency: String = "ETB",
    val sourceAccount: String? = null,
    val recipientAccount: String? = null,
    val generalAccount: String? = null,
    val counterpartyName: String? = null,
    val rawSenderName: String? = null,
    val counterpartyAccount: String? = null,
    val balance: BigDecimal? = null,
    val reference: String? = null,
    val channel: String? = null,
    val serviceCharge: BigDecimal? = null,
    val vat: BigDecimal? = null,
    val disasterRecoveryCharge: BigDecimal? = null,
    val totalDebit: BigDecimal? = null,
    val date: LocalDate,
    val time: LocalTime?
)

/**
 * Deterministic extractor for fields across supported CBE SMS message structures.
 */
object CbeTransactionExtractor {

    fun extract(
        messageType: CbeMessageType,
        senderAddress: String,
        body: String,
        smsTimestamp: Instant
    ): ExtractedSmsData? {
        val (txDate, txTime) = extractDateTime(body, smsTimestamp)
        val balance = extractBalance(body)
        val reference = extractReference(body)

        val isTelebirrSender = senderAddress.contains("telebirr", ignoreCase = true) ||
            senderAddress == "889"
        val messageMentionsTelebirr = body.contains("telebirr", ignoreCase = true)

        return when (messageType) {
            CbeMessageType.CREDIT_RECEIPT -> {
                // Pattern A: "Dear Yohannis Kefiyale your Account ...3608 has been credited with ETB 10. Your Current Balance is ETB 4243.1."
                val amount = extractAmountForCredit(body) ?: return null
                val account = extractSingleAccount(body)

                val channel = if (isTelebirrSender || messageMentionsTelebirr) "TELEBIRR" else null
                val (extractedCounterparty, rawName) = extractCreditSender(body)
                val counterpartyName = extractedCounterparty ?: "Unknown sender"

                ExtractedSmsData(
                    type = TransactionType.RECEIVED,
                    amount = amount,
                    currency = "ETB",
                    recipientAccount = account,
                    generalAccount = account,
                    counterpartyName = counterpartyName,
                    rawSenderName = rawName,
                    counterpartyAccount = null,
                    balance = balance,
                    reference = reference,
                    channel = channel,
                    date = txDate,
                    time = txTime
                )
            }

            CbeMessageType.INCOMING_TRANSFER -> {
                // Pattern B: "You have received ETB [amount] from account [sender account] ([sender name]) to your account [recipient account]. Your current balance is ETB [balance]."
                val amount = extractAmountForReceived(body) ?: extractGeneralAmount(body) ?: return null
                val transferAccounts = extractIncomingTransferAccounts(body)

                val senderName = transferAccounts.senderName?.trim()
                val counterpartyName = if (!senderName.isNullOrBlank()) senderName else "Unknown sender"
                val rawSenderName = if (!senderName.isNullOrBlank()) senderName else null

                val channel = if (isTelebirrSender || messageMentionsTelebirr) "TELEBIRR" else "TRANSFER"

                ExtractedSmsData(
                    type = TransactionType.RECEIVED,
                    amount = amount,
                    currency = "ETB",
                    sourceAccount = transferAccounts.senderAccount,
                    recipientAccount = transferAccounts.recipientAccount,
                    generalAccount = transferAccounts.recipientAccount,
                    counterpartyName = counterpartyName,
                    rawSenderName = rawSenderName,
                    counterpartyAccount = transferAccounts.senderAccount,
                    balance = balance,
                    reference = reference,
                    channel = channel,
                    date = txDate,
                    time = txTime
                )
            }

            CbeMessageType.OUTGOING_TRANSFER -> {
                // Pattern C: "You have successfully transferred ETB 310.00 from account ...3608 to account ...1626 (Gutema Mitiku Gemeda). Service charge of ETB 0.50 and VAT(15%) of ETB 0.08 and Disaster Recovery(5%) of 0.03 with total of ETB 310.61."
                val amount = extractAmountForTransferred(body) ?: return null
                val transferAccounts = extractOutgoingTransferAccounts(body)

                val serviceCharge = extractFee(body, """(?i)Service\s+charge\s+of\s+(?:ETB\s*)?([\d,]+(?:\.\d{1,2})?)""")
                val vat = extractFee(body, """(?i)VAT\s*(?:\([^)]*\))?\s*of\s+(?:ETB\s*)?([\d,]+(?:\.\d{1,2})?)""")
                val disasterRecovery = extractFee(body, """(?i)Disaster\s+Recovery\s*(?:\([^)]*\))?\s*of\s+(?:ETB\s*)?([\d,]+(?:\.\d{1,2})?)""")
                val totalDebit = extractFee(body, """(?i)total\s+(?:of\s+)?(?:ETB\s*)?([\d,]+(?:\.\d{1,2})?)""")

                ExtractedSmsData(
                    type = TransactionType.WITHDRAWAL,
                    amount = amount,
                    currency = "ETB",
                    sourceAccount = transferAccounts.senderAccount,
                    recipientAccount = transferAccounts.recipientAccount,
                    generalAccount = transferAccounts.senderAccount,
                    counterpartyName = transferAccounts.recipientName,
                    rawSenderName = transferAccounts.recipientName,
                    counterpartyAccount = transferAccounts.recipientAccount,
                    balance = balance,
                    reference = reference,
                    channel = "TRANSFER",
                    serviceCharge = serviceCharge,
                    vat = vat,
                    disasterRecoveryCharge = disasterRecovery,
                    totalDebit = totalDebit,
                    date = txDate,
                    time = txTime
                )
            }

            CbeMessageType.DEBIT_OR_WITHDRAWAL -> {
                // Pattern D: "Dear Yohannis Kefiyale your Account ...3608 has been debited with ETB 7000. Your Current Balance is ETB 1438.1."
                val amount = extractAmountForDebited(body) ?: return null
                val account = extractSingleAccount(body)

                val lower = body.lowercase(Locale.ROOT)
                val channel = when {
                    lower.contains("atm") -> "ATM"
                    lower.contains("pos") || lower.contains("purchase") -> "POS"
                    else -> null
                }

                val counterparty = when {
                    lower.contains("atm") -> {
                        Regex("""at\s+(ATM[A-Za-z0-9\s-]+)""", RegexOption.IGNORE_CASE).find(body)?.groups?.get(1)?.value ?: "ATM Cash Withdrawal"
                    }
                    else -> null
                }

                ExtractedSmsData(
                    type = TransactionType.WITHDRAWAL,
                    amount = amount,
                    currency = "ETB",
                    sourceAccount = account,
                    generalAccount = account,
                    counterpartyName = counterparty,
                    rawSenderName = counterparty,
                    counterpartyAccount = null,
                    balance = balance,
                    reference = reference,
                    channel = channel,
                    date = txDate,
                    time = txTime
                )
            }

            CbeMessageType.BRANCH_DEPOSIT -> {
                val amount = extractAmountForDeposit(body) ?: return null
                val account = extractSingleAccount(body)
                val branch = Regex("""at\s+([A-Za-z\s]+Branch)""", RegexOption.IGNORE_CASE).find(body)?.groups?.get(1)?.value

                ExtractedSmsData(
                    type = TransactionType.DEPOSIT,
                    amount = amount,
                    currency = "ETB",
                    recipientAccount = account,
                    generalAccount = account,
                    counterpartyName = branch ?: "Branch Cash Deposit",
                    rawSenderName = branch,
                    counterpartyAccount = null,
                    balance = balance,
                    reference = reference,
                    channel = "BRANCH",
                    date = txDate,
                    time = txTime
                )
            }

            CbeMessageType.UNKNOWN -> null
        }
    }

    private fun extractAmountForCredit(body: String): BigDecimal? {
        val regex = Regex("""(?i)(?:credited\s+with|transferred\s+to\s+your\s+CBE\s+Account)\s*(?:ETB|Birr)?\s*([\d,]+(?:\.\d{1,2})?)""")
        return parseAmountFromMatch(regex.find(body)) ?: extractGeneralAmount(body)
    }

    private fun extractAmountForReceived(body: String): BigDecimal? {
        val regex = Regex("""(?i)received\s*(?:ETB|Birr)?\s*([\d,]+(?:\.\d{1,2})?)""")
        return parseAmountFromMatch(regex.find(body))
    }

    private fun extractAmountForTransferred(body: String): BigDecimal? {
        val regex = Regex("""(?i)(?:successfully\s+)?transferred\s*(?:ETB|Birr)?\s*([\d,]+(?:\.\d{1,2})?)""")
        return parseAmountFromMatch(regex.find(body))
    }

    private fun extractAmountForDebited(body: String): BigDecimal? {
        val regex = Regex("""(?i)(?:debited\s+with|debited\s+from)\s*(?:ETB|Birr)?\s*([\d,]+(?:\.\d{1,2})?)""")
        return parseAmountFromMatch(regex.find(body)) ?: extractGeneralAmount(body)
    }

    private fun extractAmountForDeposit(body: String): BigDecimal? {
        val regex = Regex("""(?i)(?:deposited\s+to|deposited)\s*(?:ETB|Birr)?\s*([\d,]+(?:\.\d{1,2})?)""")
        return parseAmountFromMatch(regex.find(body)) ?: extractGeneralAmount(body)
    }

    private fun extractGeneralAmount(body: String): BigDecimal? {
        val regex = Regex("""(?i)(?<!Balance\s)(?<!Bal\s)(?<!Avail\s)(?<!is\s)(?:ETB|Birr)\s*([\d,]+(?:\.\d{1,2})?)""")
        for (match in regex.findAll(body)) {
            val parsed = parseAmountString(match.groups[1]?.value)
            if (parsed != null && parsed > BigDecimal.ZERO) {
                return parsed
            }
        }
        return null
    }

    private fun extractFee(body: String, pattern: String): BigDecimal? {
        val match = Regex(pattern).find(body) ?: return null
        return parseAmountString(match.groups[1]?.value)
    }

    private fun extractBalance(body: String): BigDecimal? {
        val regex = Regex("""(?i)(?:Current\s+Balance|Avail(?:able)?\s*Bal(?:ance)?|Balance|Bal)\s*(?:is\s*)?(?:ETB|Birr)?\s*([\d,]+(?:\.\d{1,2})?)""")
        val match = regex.find(body) ?: return null
        return parseAmountString(match.groups[1]?.value)
    }

    private fun extractReference(body: String): String? {
        val regex = Regex("""(?:Ref|Reference|Txn|TxnId|Transaction\s*ID|Ref\s*No)[:.]?\s*([A-Za-z0-9]+)""", RegexOption.IGNORE_CASE)
        val match = regex.find(body) ?: return null
        return match.groups[1]?.value
    }

    private fun extractSingleAccount(body: String): String? {
        val fullRegex = Regex("""(?i)(?:A/C|Account|Acc|Acc\.)\s*(\d{10,16})""")
        val fullMatch = fullRegex.find(body)
        if (fullMatch != null) {
            return fullMatch.groups[1]?.value
        }

        val maskedRegex = Regex("""(?i)(?:A/C|Account|Acc|Acc\.)?\s*(?:\.{2,}|\*{2,}|••••|•+)\s*(\d{4})""")
        val maskedMatch = maskedRegex.find(body)
        if (maskedMatch != null) {
            return maskedMatch.groups[1]?.value
        }

        return null
    }

    private data class TransferAccounts(
        val senderAccount: String? = null,
        val senderName: String? = null,
        val recipientAccount: String? = null,
        val recipientName: String? = null
    )

    private fun extractIncomingTransferAccounts(body: String): TransferAccounts {
        // Pattern B: "from account [sender account] ([sender name]) to your account [recipient account]"
        val regex = Regex(
            """(?i)from\s+account\s+([^\s()]+)(?:\s*\(([^)]+)\))?\s+to\s+(?:your\s+)?account\s+([^\s()]+)"""
        )
        val match = regex.find(body)
        if (match != null) {
            val senderAccount = match.groups[1]?.value?.trim()?.trimEnd('.', ',')
            val senderName = match.groups[2]?.value?.trim()
            val recipientAccount = match.groups[3]?.value?.trim()?.trimEnd('.', ',')
            return TransferAccounts(
                senderAccount = senderAccount,
                senderName = senderName,
                recipientAccount = recipientAccount
            )
        }

        return TransferAccounts(
            recipientAccount = extractSingleAccount(body)
        )
    }

    private fun extractOutgoingTransferAccounts(body: String): TransferAccounts {
        // Pattern C: "from account ...3608 to account ...1626 (Gutema Mitiku Gemeda)"
        val regex = Regex(
            """(?i)from\s+account\s+([^\s()]+)\s+to\s+account\s+([^\s()]+)(?:\s*\(([^)]+)\))?"""
        )
        val match = regex.find(body)
        if (match != null) {
            val senderAccount = match.groups[1]?.value?.trim()?.trimEnd('.', ',')
            val recipientAccount = match.groups[2]?.value?.trim()?.trimEnd('.', ',')
            val recipientName = match.groups[3]?.value?.trim()
            return TransferAccounts(
                senderAccount = senderAccount,
                recipientAccount = recipientAccount,
                recipientName = recipientName
            )
        }

        return TransferAccounts(
            senderAccount = extractSingleAccount(body)
        )
    }

    private fun extractCreditSender(body: String): Pair<String?, String?> {
        val fromRegex = Regex("""from\s+([A-Za-z0-9\s&.,'-]+?)(?=\.\s+|\s+Balance|\s+Current|\s+Avail|\s+on\s+\d|\s+Ref|$)""", RegexOption.IGNORE_CASE)
        val fromMatch = fromRegex.find(body)
        if (fromMatch != null) {
            val name = fromMatch.groups[1]?.value?.trim()?.trimEnd('.', ',')
            if (!name.isNullOrBlank() && !name.contains("CBE Account", ignoreCase = true) && !name.equals("telebirr", ignoreCase = true)) {
                return Pair(name, name)
            }
        }
        return Pair(null, null)
    }

    private fun parseAmountFromMatch(match: MatchResult?): BigDecimal? {
        if (match == null) return null
        return parseAmountString(match.groups[1]?.value)
    }

    private fun parseAmountString(raw: String?): BigDecimal? {
        if (raw.isNullOrBlank()) return null
        val clean = raw.replace(",", "").trim()
        return try {
            BigDecimal(clean).setScale(2, RoundingMode.HALF_UP)
        } catch (_: Exception) {
            null
        }
    }

    private fun extractDateTime(body: String, fallbackInstant: Instant): Pair<LocalDate, LocalTime?> {
        val dateRegex = Regex("""(?:on\s+)?(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})(?:\s+(\d{1,2}:\d{2}(?::\d{2})?(?:\s*(?:AM|PM|am|pm))?))?""")
        val match = dateRegex.find(body)

        val zonedDateTime = fallbackInstant.atZone(ZoneId.systemDefault())
        val fallbackDate = zonedDateTime.toLocalDate()
        val fallbackTime = zonedDateTime.toLocalTime()

        if (match == null) {
            return Pair(fallbackDate, fallbackTime)
        }

        val dateStr = match.groups[1]?.value
        val timeStr = match.groups[2]?.value

        val date = if (dateStr != null) {
            parseDateString(dateStr) ?: fallbackDate
        } else {
            fallbackDate
        }

        val time = if (timeStr != null) {
            parseTimeString(timeStr) ?: fallbackTime
        } else {
            fallbackTime
        }

        return Pair(date, time)
    }

    private fun parseDateString(dateStr: String): LocalDate? {
        val clean = dateStr.trim()
        val formatters = listOf(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        )
        for (formatter in formatters) {
            try {
                return LocalDate.parse(clean, formatter)
            } catch (_: Exception) {}
        }
        return null
    }

    private fun parseTimeString(timeStr: String): LocalTime? {
        val clean = timeStr.trim().uppercase()
        val formatters = listOf(
            DateTimeFormatter.ofPattern("HH:mm", Locale.US),
            DateTimeFormatter.ofPattern("H:mm", Locale.US),
            DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
            DateTimeFormatter.ofPattern("h:mm a", Locale.US)
        )
        for (formatter in formatters) {
            try {
                return LocalTime.parse(clean, formatter)
            } catch (_: Exception) {}
        }
        return null
    }
}

package com.example.hermodit.data.sms

import java.util.Locale

enum class CbeMessageType {
    /**
     * Pattern A:
     * "Dear Yohannis Kefiyale your Account ...3608 has been credited with ETB 10. Your Current Balance is ETB 4243.1."
     */
    CREDIT_RECEIPT,

    /**
     * Pattern B:
     * "You have received ETB [amount] from account [sender account] ([sender name]) to your account [recipient account]. Your current balance is ETB [balance]."
     */
    INCOMING_TRANSFER,

    /**
     * Pattern C:
     * "You have successfully transferred ETB 310.00 from account ...3608 to account ...1626 (Gutema Mitiku Gemeda). Service charge of ETB 0.50 and VAT(15%) of ETB 0.08 and Disaster Recovery(5%) of 0.03 with total of ETB 310.61."
     */
    OUTGOING_TRANSFER,

    /**
     * Pattern D:
     * "Dear Yohannis Kefiyale your Account ...3608 has been debited with ETB 7000. Your Current Balance is ETB 1438.1."
     */
    DEBIT_OR_WITHDRAWAL,

    /**
     * Branch cash deposit
     */
    BRANCH_DEPOSIT,

    UNKNOWN
}

/**
 * Deterministic SMS classification based on CBE message patterns.
 */
object CbeMessageClassifier {

    fun classify(messageBody: String): CbeMessageType {
        val lower = messageBody.lowercase(Locale.ROOT)

        // Check for Outgoing Transfer (Pattern C)
        if (lower.contains("successfully transferred") ||
            (lower.contains("transferred") && lower.contains("from account") && lower.contains("to account"))
        ) {
            return CbeMessageType.OUTGOING_TRANSFER
        }

        // Check for Incoming CBE-to-CBE Transfer (Pattern B)
        if (lower.contains("you have received") ||
            (lower.contains("received") && lower.contains("from account") && lower.contains("to your account"))
        ) {
            return CbeMessageType.INCOMING_TRANSFER
        }

        // Check for Debit / Cash Withdrawal (Pattern D)
        if (lower.contains("has been debited") ||
            lower.contains("debited with") ||
            lower.contains("debited from") ||
            lower.contains("cash withdrawal")
        ) {
            return CbeMessageType.DEBIT_OR_WITHDRAWAL
        }

        // Check for Credit Receipt (Pattern A: credited with, or transferred to your CBE account)
        if (lower.contains("has been credited") ||
            lower.contains("credited with") ||
            lower.contains("transferred to your cbe account")
        ) {
            return CbeMessageType.CREDIT_RECEIPT
        }

        // Check for Branch Deposit
        if (lower.contains("deposited to") || lower.contains("cash deposit")) {
            return CbeMessageType.BRANCH_DEPOSIT
        }

        return CbeMessageType.UNKNOWN
    }
}

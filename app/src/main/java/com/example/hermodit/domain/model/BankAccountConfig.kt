package com.example.hermodit.domain.model

import java.time.Instant

/**
 * Model representing the single monitored bank account configuration.
 *
 * Monitored account:
 * Bank: Commercial Bank of Ethiopia (CBE)
 * Account number: 1000763003608
 * Account holder: YOANNIS KEFYALEW AND MEKLIT ASHENAF
 */
data class BankAccountConfig(
    val bankName: String = "Commercial Bank of Ethiopia",
    val bankCode: String = "CBE",
    val accountIdentifier: String = "•••• 3608",
    val fullAccountNumber: String = "1000763003608",
    val accountHolder: String = "YOANNIS KEFYALEW AND MEKLIT ASHENAF",
    val accountType: String = "SAVINGS",
    val authorizedSenderIds: List<String> = listOf("CBE", "CBE_Birr", "889", "telebirr"),
    val isSmsShieldActive: Boolean = true,
    val lastSyncTime: Instant? = null
)

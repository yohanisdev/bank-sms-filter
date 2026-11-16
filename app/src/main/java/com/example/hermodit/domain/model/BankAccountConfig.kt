package com.example.hermodit.domain.model

import java.time.Instant

/**
 * Model representing the single monitored bank account configuration.
 */
data class BankAccountConfig(
    val bankName: String = "Commercial Bank of Ethiopia",
    val bankCode: String = "CBE",
    val accountIdentifier: String = "•••• 4582",
    val fullAccountNumber: String = "1000123454582",
    val accountType: String = "SAVINGS",
    val authorizedSenderIds: List<String> = listOf("CBE", "CBE_Birr", "889"),
    val isSmsShieldActive: Boolean = true,
    val lastSyncTime: Instant = Instant.now()
)

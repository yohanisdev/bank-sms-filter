package com.example.hermodit.domain.model

import java.time.Instant

/**
 * Domain record for SMS messages that could not be reliably verified as belonging
 * to the monitored account or that require manual user review.
 */
data class UnmatchedSmsRecord(
    val id: String,
    val senderAddress: String,
    val messageBody: String,
    val receivedTimestamp: Instant,
    val reason: String
)

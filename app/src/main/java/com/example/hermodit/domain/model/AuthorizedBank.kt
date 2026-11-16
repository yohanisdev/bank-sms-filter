package com.example.hermodit.domain.model

/**
 * Entity representing an authorized financial institution whose SMS notifications
 * are tracked by the application.
 */
data class AuthorizedBank(
    val id: String,
    val bankName: String,
    val senderAddress: String,
    val countryCode: String = "US",
    val isActive: Boolean = true
)

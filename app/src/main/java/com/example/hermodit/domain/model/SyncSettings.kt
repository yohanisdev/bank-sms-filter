package com.example.hermodit.domain.model

/**
 * Model representing synchronization and telemetry preferences.
 */
data class SyncSettings(
    val automaticSyncEnabled: Boolean = true,
    val backgroundWorkerActive: Boolean = true,
    val retryAttempts: Int = 3,
    val lastSuccessfulSync: String = "Today, 10:42 AM",
    val auditLoggingEnabled: Boolean = true,
    val localStorageEncrypted: Boolean = true,
    val oditApiIntegrationEnabled: Boolean = false,
    val oditApiEndpoint: String = "https://api.odit.internal/v1/sync",
    val oditApiKeyConfigured: Boolean = false
)

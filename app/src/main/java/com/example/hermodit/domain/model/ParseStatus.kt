package com.example.hermodit.domain.model

/**
 * Status of a parsed SMS transaction.
 */
enum class ParseStatus {
    /** Successfully verified against the monitored bank account and valid structure. */
    CONFIRMED,

    /** Needs manual user confirmation (e.g. name mentioned but missing account number). */
    REQUIRES_REVIEW,

    /** Account number does not match the configured monitored account. */
    UNMATCHED
}

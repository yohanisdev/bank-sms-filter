package com.example.hermodit.data.sms

import com.example.hermodit.domain.repository.TransactionRepository

/**
 * Deduplication validator using stable sourceIdentifier and bankReferenceNumber.
 * Never relies on amount and date alone.
 */
class SmsDeduplicator(
    private val repository: TransactionRepository
) {
    suspend fun isDuplicate(sourceIdentifier: String, bankReferenceNumber: String?): Boolean {
        return repository.exists(sourceIdentifier, bankReferenceNumber)
    }
}

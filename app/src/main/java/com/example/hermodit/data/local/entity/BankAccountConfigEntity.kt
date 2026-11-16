package com.example.hermodit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hermodit.domain.model.BankAccountConfig
import java.time.Instant

@Entity(tableName = "bank_account_config")
data class BankAccountConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val bankName: String,
    val bankCode: String,
    val accountIdentifier: String,
    val fullAccountNumber: String,
    val accountHolder: String,
    val accountType: String,
    val authorizedSenderIds: String, // Comma-separated list
    val isSmsShieldActive: Boolean,
    val lastSyncTimestamp: Long?
) {
    fun toDomain(): BankAccountConfig = BankAccountConfig(
        bankName = bankName,
        bankCode = bankCode,
        accountIdentifier = accountIdentifier,
        fullAccountNumber = fullAccountNumber,
        accountHolder = accountHolder,
        accountType = accountType,
        authorizedSenderIds = authorizedSenderIds.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        isSmsShieldActive = isSmsShieldActive,
        lastSyncTime = lastSyncTimestamp?.let { Instant.ofEpochMilli(it) }
    )

    companion object {
        fun fromDomain(domain: BankAccountConfig): BankAccountConfigEntity = BankAccountConfigEntity(
            id = 1,
            bankName = domain.bankName,
            bankCode = domain.bankCode,
            accountIdentifier = domain.accountIdentifier,
            fullAccountNumber = domain.fullAccountNumber,
            accountHolder = domain.accountHolder,
            accountType = domain.accountType,
            authorizedSenderIds = domain.authorizedSenderIds.joinToString(","),
            isSmsShieldActive = domain.isSmsShieldActive,
            lastSyncTimestamp = domain.lastSyncTime?.toEpochMilli()
        )
    }
}

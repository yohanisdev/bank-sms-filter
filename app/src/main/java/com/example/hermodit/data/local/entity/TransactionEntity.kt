package com.example.hermodit.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.hermodit.domain.model.FinancialTransaction
import com.example.hermodit.domain.model.ParseStatus
import com.example.hermodit.domain.model.TransactionType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["sourceIdentifier"], unique = true),
        Index(value = ["bankReferenceNumber"], unique = false),
        Index(value = ["transactionDate"], unique = false)
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val bankName: String,
    val accountIdentifier: String,
    val type: TransactionType,
    val amount: BigDecimal,
    val currency: String,
    val counterpartyName: String?,
    val counterpartyAccount: String?,
    val rawSenderName: String? = null,
    val transactionDate: LocalDate,
    val transactionTime: LocalTime?,
    val availableBalance: BigDecimal?,
    val bankReferenceNumber: String?,
    val rawSmsContent: String?,
    val sourceIdentifier: String,
    val smsTimestamp: Instant,
    val smsSenderId: String,
    val channel: String?,
    val serviceCharge: BigDecimal? = null,
    val vat: BigDecimal? = null,
    val disasterRecoveryCharge: BigDecimal? = null,
    val totalDebit: BigDecimal? = null,
    val parseStatus: ParseStatus = ParseStatus.CONFIRMED
) {
    fun toDomain(): FinancialTransaction = FinancialTransaction(
        id = id,
        bankName = bankName,
        accountIdentifier = accountIdentifier,
        type = type,
        amount = amount,
        currency = currency,
        counterpartyName = counterpartyName,
        counterpartyAccount = counterpartyAccount,
        rawSenderName = rawSenderName,
        transactionDate = transactionDate,
        transactionTime = transactionTime,
        availableBalance = availableBalance,
        bankReferenceNumber = bankReferenceNumber,
        rawSmsContent = rawSmsContent,
        sourceIdentifier = sourceIdentifier,
        smsTimestamp = smsTimestamp,
        smsSenderId = smsSenderId,
        channel = channel,
        serviceCharge = serviceCharge,
        vat = vat,
        disasterRecoveryCharge = disasterRecoveryCharge,
        totalDebit = totalDebit,
        parseStatus = parseStatus
    )

    companion object {
        fun fromDomain(domain: FinancialTransaction): TransactionEntity = TransactionEntity(
            id = domain.id,
            bankName = domain.bankName,
            accountIdentifier = domain.accountIdentifier,
            type = domain.type,
            amount = domain.amount,
            currency = domain.currency,
            counterpartyName = domain.counterpartyName,
            counterpartyAccount = domain.counterpartyAccount,
            rawSenderName = domain.rawSenderName,
            transactionDate = domain.transactionDate,
            transactionTime = domain.transactionTime,
            availableBalance = domain.availableBalance,
            bankReferenceNumber = domain.bankReferenceNumber,
            rawSmsContent = domain.rawSmsContent,
            sourceIdentifier = domain.sourceIdentifier,
            smsTimestamp = domain.smsTimestamp,
            smsSenderId = domain.smsSenderId,
            channel = domain.channel,
            serviceCharge = domain.serviceCharge,
            vat = domain.vat,
            disasterRecoveryCharge = domain.disasterRecoveryCharge,
            totalDebit = domain.totalDebit,
            parseStatus = domain.parseStatus
        )
    }
}

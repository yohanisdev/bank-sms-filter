package com.example.hermodit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hermodit.domain.model.UnmatchedSmsRecord
import java.time.Instant

@Entity(tableName = "unmatched_sms")
data class UnmatchedSmsEntity(
    @PrimaryKey
    val id: String,
    val senderAddress: String,
    val messageBody: String,
    val receivedTimestamp: Instant,
    val reason: String
) {
    fun toDomain(): UnmatchedSmsRecord = UnmatchedSmsRecord(
        id = id,
        senderAddress = senderAddress,
        messageBody = messageBody,
        receivedTimestamp = receivedTimestamp,
        reason = reason
    )

    companion object {
        fun fromDomain(domain: UnmatchedSmsRecord): UnmatchedSmsEntity = UnmatchedSmsEntity(
            id = domain.id,
            senderAddress = domain.senderAddress,
            messageBody = domain.messageBody,
            receivedTimestamp = domain.receivedTimestamp,
            reason = domain.reason
        )
    }
}

package com.example.hermodit.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.hermodit.data.datasource.RoomTransactionDataSource
import com.example.hermodit.data.local.HermoditDatabase
import com.example.hermodit.data.repository.TransactionRepositoryImpl
import com.example.hermodit.data.sms.CbeSmsParser
import com.example.hermodit.data.sms.ParseResult
import com.example.hermodit.domain.model.BankAccountConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Real-time SMS broadcast receiver that processes incoming bank messages.
 * Reuses the exact same parser, account matching, and deduplication pipeline
 * as the historical scanner.
 */
class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) {
            return
        }

        val senderAddress = messages[0].originatingAddress ?: return
        val messageBody = messages.joinToString("") { it.messageBody ?: "" }
        val timestamp = Instant.ofEpochMilli(messages[0].timestampMillis)
        val smsId = "rt-${messages[0].timestampMillis}"

        val parser = CbeSmsParser()
        if (!parser.canHandle(senderAddress, messageBody)) {
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = HermoditDatabase.getInstance(context)
                val dataSource = RoomTransactionDataSource(db.transactionDao())
                val repository = TransactionRepositoryImpl(
                    localDataSource = dataSource,
                    configDao = db.bankAccountConfigDao(),
                    unmatchedSmsDao = db.unmatchedSmsDao()
                )

                val config = repository.getMonitoredAccountConfig().first()

                when (val result = parser.parse(smsId, senderAddress, messageBody, timestamp, config)) {
                    is ParseResult.Success -> {
                        val tx = result.transaction
                        val isDuplicate = repository.exists(tx.sourceIdentifier, tx.bankReferenceNumber)
                        if (!isDuplicate) {
                            repository.addTransaction(tx)
                        }
                    }
                    is ParseResult.Ambiguous -> {
                        repository.recordUnmatchedSms(result.record)
                    }
                    is ParseResult.AccountMismatch, is ParseResult.Ignored, is ParseResult.InvalidFormat -> {
                        // Ignore non-matching or invalid messages
                    }
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}

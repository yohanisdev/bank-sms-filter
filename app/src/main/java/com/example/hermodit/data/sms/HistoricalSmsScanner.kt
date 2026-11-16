package com.example.hermodit.data.sms

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.example.hermodit.domain.model.BankAccountConfig
import com.example.hermodit.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

data class ScanProgress(
    val scannedCount: Int = 0,
    val matchedCount: Int = 0,
    val savedCount: Int = 0,
    val duplicateCount: Int = 0,
    val ignoredCount: Int = 0,
    val ambiguousCount: Int = 0,
    val isScanning: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Historical SMS inbox scanner that reads bank notifications, applies
 * strict account verification, deduplication, and persists confirmed records.
 */
class HistoricalSmsScanner(
    private val repository: TransactionRepository,
    private val parser: SmsParser = CbeSmsParser()
) {

    suspend fun scanInbox(
        context: Context,
        config: BankAccountConfig,
        onProgress: (ScanProgress) -> Unit = {}
    ): ScanProgress = withContext(Dispatchers.IO) {
        // 1. Check SMS permission
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            val error = ScanProgress(
                isScanning = false,
                errorMessage = "SMS read permission not granted (android.permission.READ_SMS)"
            )
            onProgress(error)
            return@withContext error
        }

        var progress = ScanProgress(isScanning = true)
        onProgress(progress)

        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date")
        val sortOrder = "date DESC"

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )

            if (cursor == null) {
                progress = progress.copy(isScanning = false, errorMessage = "Unable to access SMS Content Provider")
                onProgress(progress)
                return@withContext progress
            }

            cursor.use { c ->
                val idCol = c.getColumnIndexOrThrow("_id")
                val addressCol = c.getColumnIndexOrThrow("address")
                val bodyCol = c.getColumnIndexOrThrow("body")
                val dateCol = c.getColumnIndexOrThrow("date")

                while (c.moveToNext()) {
                    val id = c.getString(idCol) ?: ""
                    val address = c.getString(addressCol) ?: ""
                    val body = c.getString(bodyCol) ?: ""
                    val dateMillis = c.getLong(dateCol)
                    val timestamp = Instant.ofEpochMilli(dateMillis)

                    progress = progress.copy(scannedCount = progress.scannedCount + 1)

                    if (!parser.canHandle(address, body)) {
                        progress = progress.copy(ignoredCount = progress.ignoredCount + 1)
                        onProgress(progress)
                        continue
                    }

                    when (val result = parser.parse(id, address, body, timestamp, config)) {
                        is ParseResult.Success -> {
                            val tx = result.transaction
                            progress = progress.copy(matchedCount = progress.matchedCount + 1)

                            // Check deduplication
                            val isDuplicate = repository.exists(tx.sourceIdentifier, tx.bankReferenceNumber)
                            if (isDuplicate) {
                                progress = progress.copy(duplicateCount = progress.duplicateCount + 1)
                            } else {
                                repository.addTransaction(tx)
                                progress = progress.copy(savedCount = progress.savedCount + 1)
                            }
                        }
                        is ParseResult.Ambiguous -> {
                            progress = progress.copy(ambiguousCount = progress.ambiguousCount + 1)
                            repository.recordUnmatchedSms(result.record)
                        }
                        is ParseResult.AccountMismatch -> {
                            progress = progress.copy(ignoredCount = progress.ignoredCount + 1)
                        }
                        is ParseResult.Ignored, is ParseResult.InvalidFormat -> {
                            progress = progress.copy(ignoredCount = progress.ignoredCount + 1)
                        }
                    }

                    // Throttle updates periodically
                    if (progress.scannedCount % 5 == 0) {
                        onProgress(progress)
                    }
                }
            }

            progress = progress.copy(isScanning = false)
            onProgress(progress)
            progress
        } catch (e: Exception) {
            val error = progress.copy(
                isScanning = false,
                errorMessage = "Scanning error: ${e.localizedMessage ?: "Unknown error"}"
            )
            onProgress(error)
            error
        }
    }
}

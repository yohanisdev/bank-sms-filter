package com.example.hermodit.domain.repository

import com.example.hermodit.domain.model.AuthorizedBank
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing authorized bank senders.
 */
interface BankSenderRepository {
    fun getAuthorizedBanks(): Flow<List<AuthorizedBank>>
    suspend fun isAuthorizedSender(senderAddress: String): Boolean
    suspend fun addAuthorizedBank(bank: AuthorizedBank)
}

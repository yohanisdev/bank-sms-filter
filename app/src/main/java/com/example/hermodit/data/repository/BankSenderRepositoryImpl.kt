package com.example.hermodit.data.repository

import com.example.hermodit.domain.model.AuthorizedBank
import com.example.hermodit.domain.repository.BankSenderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory repository managing authorized bank senders.
 */
class BankSenderRepositoryImpl : BankSenderRepository {
    private val mutex = Mutex()
    private val banksFlow = MutableStateFlow<List<AuthorizedBank>>(defaultBanks())

    override fun getAuthorizedBanks(): Flow<List<AuthorizedBank>> = banksFlow.asStateFlow()

    override suspend fun isAuthorizedSender(senderAddress: String): Boolean {
        val normalized = senderAddress.trim().uppercase()
        return mutex.withLock {
            banksFlow.value.any { it.isActive && it.senderAddress.uppercase() == normalized }
        }
    }

    override suspend fun addAuthorizedBank(bank: AuthorizedBank) {
        mutex.withLock {
            val current = banksFlow.value.toMutableList()
            current.removeAll { it.id == bank.id }
            current.add(bank)
            banksFlow.value = current
        }
    }

    companion object {
        private fun defaultBanks(): List<AuthorizedBank> = listOf(
            AuthorizedBank("bank-1", "JPMorgan Chase", "CHASE", "US", true),
            AuthorizedBank("bank-2", "Bank of America", "BOFA", "US", true),
            AuthorizedBank("bank-3", "Citibank", "CITI", "US", true),
            AuthorizedBank("bank-4", "Wells Fargo", "WELLSFARGO", "US", true)
        )
    }
}

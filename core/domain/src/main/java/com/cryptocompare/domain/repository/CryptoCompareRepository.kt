package com.cryptocompare.domain.repository

import com.cryptocompare.model.Provider
import com.cryptocompare.model.Symbol
import kotlinx.coroutines.flow.Flow

interface CryptoCompareRepository {
    suspend fun getProviders(): Result<List<Provider>>

    fun getSymbols(): Flow<List<Symbol>>

    suspend fun getSymbolsByTicker(ticker: String): Result<List<Symbol>>

    suspend fun refreshCatalog(): Result<Unit>
}

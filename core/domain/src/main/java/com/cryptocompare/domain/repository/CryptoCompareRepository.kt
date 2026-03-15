package com.cryptocompare.domain.repository

import com.cryptocompare.model.Provider
import com.cryptocompare.model.Symbol
import kotlinx.coroutines.flow.Flow

interface CryptoCompareRepository {
    suspend fun getProviders(): Result<List<Provider>>

    suspend fun getSymbols(): Flow<List<Symbol>>
}

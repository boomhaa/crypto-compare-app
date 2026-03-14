package com.example.domain.repository

import com.example.model.Provider
import com.example.model.Symbol
import kotlinx.coroutines.flow.Flow

interface CryptoCompareRepository {
    suspend fun getProviders(): Result<List<Provider>>

    suspend fun getSymbols(): Flow<List<Symbol>>
}

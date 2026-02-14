package com.example.domain.repository

import com.example.model.Provider
import com.example.model.Symbol

interface CryptoCompareRepository {
    suspend fun getProviders(): Result<List<Provider>>

    suspend fun getSymbols(): Result<List<Symbol>>
}

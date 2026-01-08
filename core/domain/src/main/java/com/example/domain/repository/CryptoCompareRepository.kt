package com.example.domain.repository

import com.example.model.Provider

interface CryptoCompareRepository {
    suspend fun getProviders(): Result<List<Provider>>
}

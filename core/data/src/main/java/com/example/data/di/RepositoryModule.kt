package com.example.data.di

import com.example.data.repository.CryptoCompareRepositoryImpl
import com.example.domain.repository.CryptoCompareRepository
import com.example.network.api.CryptoCompareApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideCryptoCompareRepository(api: CryptoCompareApi): CryptoCompareRepository =
        CryptoCompareRepositoryImpl(api)
}

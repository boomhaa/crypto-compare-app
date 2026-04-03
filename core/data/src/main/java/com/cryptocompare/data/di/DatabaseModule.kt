package com.cryptocompare.data.di

import android.content.Context
import androidx.room.Room
import com.cryptocompare.data.local.CryptoCompareDatabase
import com.cryptocompare.data.local.dao.ProviderDao
import com.cryptocompare.data.local.dao.SymbolDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideCryptoCompareDatabase(
        @ApplicationContext context: Context,
    ): CryptoCompareDatabase =
        Room
            .databaseBuilder(
                context,
                CryptoCompareDatabase::class.java,
                "crypto_compare.db",
            ).fallbackToDestructiveMigration(false)
            .build()

    @Provides
    fun provideProviderDao(database: CryptoCompareDatabase): ProviderDao = database.providerDao()

    @Provides
    fun provideSymbolDao(database: CryptoCompareDatabase): SymbolDao = database.symbolDao()
}

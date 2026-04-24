package com.cryptocompare.network.di

import com.cryptocompare.network.BuildConfig
import com.cryptocompare.network.api.CryptoCompareApi
import com.cryptocompare.network.api.CryptoHistoryApi
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    @Named("baseUrl")
    fun provideBaseUrl(): String = BuildConfig.BASE_URL

    @Provides
    @Singleton
    @Named("tickerHistoryUrl")
    fun provideTickerHistoryUrl(): String = BuildConfig.TICKER_HISTORY_URL

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    @Named("ioDispatcher")
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .pingInterval(20, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideCryptoCompareApi(
        gson: Gson,
        @Named("baseUrl") baseUrl: String,
    ): CryptoCompareApi =
        Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CryptoCompareApi::class.java)

    @Provides
    @Singleton
    fun provideCryptoHistoryApo(
        gson: Gson,
        @Named("tickerHistoryUrl") tickerHistoryUrl: String,
    ): CryptoHistoryApi =
        Retrofit
            .Builder()
            .baseUrl(tickerHistoryUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CryptoHistoryApi::class.java)
}

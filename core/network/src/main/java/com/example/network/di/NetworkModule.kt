package com.example.network.di

import com.example.network.BuildConfig
import com.example.network.api.CryptoCompareApi
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    fun provideWebSocketUrl(): String = BuildConfig.BASE_URL

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

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
}

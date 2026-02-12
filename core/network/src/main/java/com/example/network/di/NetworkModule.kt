package com.example.network.di

import android.content.Context
import java.util.concurrent.TimeUnit
import com.example.network.R
import com.example.network.api.CryptoCompareApi
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
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
        @ApplicationContext context: Context,
        gson: Gson
    ): CryptoCompareApi {
        val baseUrl = context.getString(R.string.base_url)

        return Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CryptoCompareApi::class.java)
    }
}

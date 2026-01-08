package com.example.network.di

import android.content.Context
import com.example.network.R
import com.example.network.api.CryptoCompareApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideCryptoCompareApi(
        @ApplicationContext context: Context,
    ): CryptoCompareApi {
        val baseUrl = context.getString(R.string.base_url)

        return Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CryptoCompareApi::class.java)
    }
}

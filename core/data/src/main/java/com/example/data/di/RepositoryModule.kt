package com.example.data.di

import com.example.data.BuildConfig
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.CryptoCompareRepositoryImpl
import com.example.data.repository.TickerStreamRepositoryImpl
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.CryptoCompareRepository
import com.example.domain.repository.TickerStreamRepository
import com.example.network.api.CryptoCompareApi
import com.example.network.websocket.WebSocketClient
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    @Named("wsUrl")
    fun provideWebSocketUrl(): String = BuildConfig.WS_BASE_URL

    @Provides
    @Singleton
    fun provideCryptoCompareRepository(api: CryptoCompareApi): CryptoCompareRepository =
        CryptoCompareRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository = AuthRepositoryImpl(auth)

    @Provides
    @Singleton
    fun provideTickerStreamRepository(
        webSocketClient: WebSocketClient,
        @Named("wsUrl") wsUrl: String,
    ): TickerStreamRepository = TickerStreamRepositoryImpl(webSocketClient, wsUrl)
}

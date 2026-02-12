package com.example.domain.repository

import com.example.model.TickerConnectionState
import com.example.model.TickerStreamEvent
import kotlinx.coroutines.flow.Flow

interface TickerStreamRepository {
    val connectionState: Flow<TickerConnectionState>
    val event: Flow<TickerStreamEvent>

    fun connect(url: String)
    fun disconnect()

    fun subscribe(ticker: String): Boolean
    fun unsubscribe(ticker: String): Boolean
}

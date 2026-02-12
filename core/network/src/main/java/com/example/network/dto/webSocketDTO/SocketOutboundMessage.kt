package com.example.network.dto.webSocketDTO

import com.example.network.dto.webSocketDTO.dataTypes.TickerData

data class SocketOutboundMessage(
    val type: Int,
    val data: TickerData
)

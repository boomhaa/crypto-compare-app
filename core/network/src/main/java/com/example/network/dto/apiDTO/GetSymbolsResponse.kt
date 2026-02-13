package com.example.network.dto.apiDTO

data class GetSymbolsResponse(
    val errorCode: Int,
    val errorMsgs: List<String>?,
    val symbols: List<SymbolDto>?,
)

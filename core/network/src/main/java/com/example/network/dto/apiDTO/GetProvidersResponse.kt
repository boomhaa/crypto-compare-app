package com.example.network.dto.apiDTO

data class GetProvidersResponse(
    val errorCode: Int,
    val errorMsgs: List<String>?,
    val providers: List<ProviderDto>?,
)

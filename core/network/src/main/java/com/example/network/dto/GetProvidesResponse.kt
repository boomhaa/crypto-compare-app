package com.example.network.dto

data class GetProvidesResponse(
    val errorCode: Int,
    val errorMsgs: List<String>?,
    val providers: List<ProviderDto>?,
)

package com.example.network.dto.apiDTO

import com.example.model.ProviderStatus

data class GetProviderResponse(
    val errorCode: Int,
    val errorMsgs: List<String>?,
    val id: Int,
    val name: String?,
    val webSite: String?,
    val baseUrl: String?,
    val accessKey: String?,
    val secretKey: String?,
    val status: ProviderStatus,
    val createdAt: String,
    val updatedAt: String,
)

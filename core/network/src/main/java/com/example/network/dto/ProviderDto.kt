package com.example.network.dto

import com.example.model.ProviderStatus

data class ProviderDto(
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

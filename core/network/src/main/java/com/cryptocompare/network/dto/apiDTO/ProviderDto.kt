package com.cryptocompare.network.dto.apiDTO

import com.cryptocompare.model.ProviderStatus

data class ProviderDto(
    val id: Int,
    val name: String?,
    val webSite: String?,
    val baseUrl: String?,
    val status: ProviderStatus,
)

package com.cryptocompare.model

data class Provider(
    val id: Int,
    val name: String?,
    val webSite: String?,
    val status: ProviderStatus,
)

package com.example.data.mapper

import com.example.model.Provider
import com.example.network.dto.ProviderDto

fun ProviderDto.toDomain(): Provider =
    Provider(
        id = id,
        name = name.orEmpty(),
        webSite = webSite,
        status = status,
    )

fun List<ProviderDto>.toDomain(): List<Provider> = map { it.toDomain() }

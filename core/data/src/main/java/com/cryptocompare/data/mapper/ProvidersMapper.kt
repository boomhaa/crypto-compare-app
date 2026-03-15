package com.cryptocompare.data.mapper

import com.cryptocompare.model.Provider
import com.cryptocompare.network.dto.apiDTO.ProviderDto

fun ProviderDto.toDomain(): Provider =
    Provider(
        id = id,
        name = name.orEmpty(),
        webSite = webSite,
        status = status,
    )

fun List<ProviderDto>.toDomain(): List<Provider> = map { it.toDomain() }

package com.cryptocompare.data.mapper

import com.cryptocompare.data.local.entity.ProviderEntity
import com.cryptocompare.model.Provider
import com.cryptocompare.model.ProviderStatus
import com.cryptocompare.network.dto.apiDTO.ProviderDto

fun ProviderDto.toDomain(): Provider =
    Provider(
        id = id,
        name = name.orEmpty(),
        webSite = webSite,
        status = status,
    )

fun List<ProviderDto>.toDomain(): List<Provider> = map(ProviderDto::toDomain)

fun ProviderEntity.toDomain(): Provider =
    Provider(
        id = id,
        name = name,
        webSite = website,
        status = ProviderStatus.valueOf(status),
    )

fun ProviderDto.toEntity(syncedAtMillis: Long): ProviderEntity =
    ProviderEntity(
        id = id,
        name = name,
        website = webSite,
        status = status.name,
        syncedAtMillis = syncedAtMillis,
    )

fun List<ProviderEntity>.toDomain(): List<Provider> = map(ProviderEntity::toDomain)

fun List<ProviderDto>.toEntity(syncedAtMillis: Long): List<ProviderEntity> =
    map {
        it.toEntity(syncedAtMillis)
    }

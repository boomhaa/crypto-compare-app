package com.cryptocompare.data.mapper

import com.cryptocompare.data.local.entity.ProviderEntity
import com.cryptocompare.model.Provider
import com.cryptocompare.model.ProviderStatus
import com.cryptocompare.network.dto.apiDTO.ProviderDto

fun ProviderEntity.toDomainFromEntity(): Provider =
    Provider(
        id = id,
        name = name,
        webSite = website,
        status = ProviderStatus.valueOf(status),
    )

fun ProviderDto.toEntityFromDomain(syncedAtMillis: Long): ProviderEntity =
    ProviderEntity(
        id = id,
        name = name,
        website = webSite,
        status = status.name,
        syncedAtMillis = syncedAtMillis,
    )

fun List<ProviderEntity>.toDomainFromEntity(): List<Provider> = map(ProviderEntity::toDomainFromEntity)

fun List<ProviderDto>.toEntityFromDomain(syncedAtMillis: Long): List<ProviderEntity> =
    map {
        it.toEntityFromDomain(syncedAtMillis)
    }

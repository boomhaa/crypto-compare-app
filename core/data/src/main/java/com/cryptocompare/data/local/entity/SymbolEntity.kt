package com.cryptocompare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "symbols",
    foreignKeys = [
        ForeignKey(
            entity = ProviderEntity::class,
            parentColumns = ["id"],
            childColumns = ["providerSellId"],
            onDelete = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = ProviderEntity::class,
            parentColumns = ["id"],
            childColumns = ["providerBuyId"],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["providerSellId"]), Index(value = ["providerBuyId"])],
)
data class SymbolEntity(
    @PrimaryKey
    val id: Long,
    val ticker: String?,
    val symbol: String?,
    val providerSellId: Int,
    val priceSell: Double,
    val providerBuyId: Int,
    val priceBuy: Double,
    val updatedAt: String,
    val syncedAtMillis: Long,
)

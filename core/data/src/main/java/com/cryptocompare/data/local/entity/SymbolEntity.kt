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
            childColumns = ["providerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["providerId"])],
)
data class SymbolEntity(
    @PrimaryKey
    val id: Long,
    val ticker: String?,
    val symbol: String?,
    val providerId: Int,
    val priceBuy: Double,
    val priceSell: Double,
    val updatedAt: String,
    val syncedAtMillis: Long,
)

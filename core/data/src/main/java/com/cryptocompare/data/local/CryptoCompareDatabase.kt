package com.cryptocompare.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cryptocompare.data.local.dao.FavouriteTickerDao
import com.cryptocompare.data.local.dao.ProviderDao
import com.cryptocompare.data.local.dao.SymbolDao
import com.cryptocompare.data.local.entity.FavouriteTickerEntity
import com.cryptocompare.data.local.entity.ProviderEntity
import com.cryptocompare.data.local.entity.SymbolEntity

@Database(
    entities = [SymbolEntity::class, ProviderEntity::class, FavouriteTickerEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class CryptoCompareDatabase : RoomDatabase() {
    abstract fun symbolDao(): SymbolDao

    abstract fun providerDao(): ProviderDao

    abstract fun favouriteTickerDao(): FavouriteTickerDao
}

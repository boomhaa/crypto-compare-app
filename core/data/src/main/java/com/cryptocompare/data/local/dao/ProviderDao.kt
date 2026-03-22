package com.cryptocompare.data.local.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cryptocompare.data.local.entity.ProviderEntity

interface ProviderDao {
    @Query("SELECT * FROM providers ORDER BY id ASC")
    suspend fun getAll(): List<ProviderEntity>

    @Query("DELETE FROM providers")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(providers: List<ProviderEntity>)

    @Query("DELETE FROM providers WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Long>)

    suspend fun syncProviders(providers: List<ProviderEntity>) {
        if (providers.isEmpty()) {
            deleteAll()
            return
        }

        upsertAll(providers)
        deleteAllExcept(providers.map(ProviderEntity::id))
    }
}

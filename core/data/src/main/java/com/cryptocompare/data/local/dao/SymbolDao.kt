package com.cryptocompare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.cryptocompare.data.local.entity.SymbolEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SymbolDao {
    @Query("SELECT * FROM symbols ORDER BY id ASC")
    suspend fun getAll(): List<SymbolEntity>

    @Query("SELECT * FROM symbols ORDER BY id ASC")
    fun observeAll(): Flow<List<SymbolEntity>>

    @Query("DELETE FROM symbols")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(symbols: List<SymbolEntity>)

    @Query("DELETE FROM symbols WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Long>)

    @Query("SELECT MAX(syncedAtMillis) FROM symbols")
    suspend fun getLastUpdate(): Long

    @Transaction
    suspend fun syncSymbols(symbols: List<SymbolEntity>) {
        if (symbols.isEmpty()) {
            deleteAll()
            return
        }

        upsertAll(symbols)
        deleteAllExcept(symbols.map(SymbolEntity::id))
    }
}

package com.cryptocompare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cryptocompare.data.local.entity.SymbolEntity

@Dao
interface SymbolDao {
    @Query("SELECT * FROM symbols ORDER BY id ASC")
    suspend fun getAll(): List<SymbolEntity>

    @Query("DELETE FROM symbols")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(symbols: List<SymbolEntity>)

    @Query("DELETE FROM symbols WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Long>)

    suspend fun syncSymbols(symbols: List<SymbolEntity>){
        if (symbols.isEmpty()){
            deleteAll()
            return
        }

        upsertAll(symbols)
        deleteAllExcept(symbols.map(SymbolEntity::id))
    }
}

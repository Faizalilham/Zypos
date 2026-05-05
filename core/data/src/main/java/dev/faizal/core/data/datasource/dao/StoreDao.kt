package dev.faizal.core.data.datasource.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.faizal.core.data.datasource.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {

    @Query("SELECT * FROM store WHERE id = 1 LIMIT 1")
    fun observeSettings(): Flow<StoreEntity?>

    @Query("SELECT * FROM store WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): StoreEntity?

    @Upsert
    suspend fun upsert(settings: StoreEntity)

    @Query("DELETE FROM store")
    suspend fun clearAll()
}
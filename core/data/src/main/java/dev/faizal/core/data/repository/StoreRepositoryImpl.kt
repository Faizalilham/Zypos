package dev.faizal.core.data.repository

import dev.faizal.core.data.datasource.dao.StoreDao
import dev.faizal.core.data.mapper.StoreMapper.toDomain
import dev.faizal.core.data.mapper.StoreMapper.toEntity
import dev.faizal.core.domain.model.store.Store
import dev.faizal.core.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StoreRepositoryImpl @Inject constructor(
    private val dao: StoreDao,
) : StoreRepository {

    override fun observeSettings(): Flow<Store?> =
        dao.observeSettings().map { it?.toDomain() }

    override suspend fun getSettings(): Store? =
        dao.getSettings()?.toDomain()

    override suspend fun saveSettings(settings: Store): Result<Unit> {
        return try {
            val updated = settings.copy(updatedAt = System.currentTimeMillis())
            dao.upsert(updated.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearSettings(): Result<Unit> {
        return try {
            dao.clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
package dev.faizal.core.domain.repository

import dev.faizal.core.domain.model.store.Store
import kotlinx.coroutines.flow.Flow

interface StoreRepository {

    /**
     * Observe pengaturan toko. Emit `null` kalau user belum onboarding.
     */
    fun observeSettings(): Flow<Store?>

    /**
     * Get sekali (snapshot). Return null kalau belum ada.
     */
    suspend fun getSettings(): Store?

    /**
     * Simpan/update pengaturan toko.
     * Pakai upsert — kalau row id=1 sudah ada, akan di-update.
     */
    suspend fun saveSettings(settings: Store): Result<Unit>

    /**
     * Hapus settings (untuk testing/reset).
     */
    suspend fun clearSettings(): Result<Unit>
}
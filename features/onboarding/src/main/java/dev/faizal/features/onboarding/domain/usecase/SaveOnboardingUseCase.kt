package dev.faizal.features.onboarding.domain.usecase

import dev.faizal.core.domain.model.store.Store
import dev.faizal.core.domain.repository.CategoryRepository
import dev.faizal.core.domain.repository.StoreRepository
import dev.faizal.features.onboarding.domain.model.OnboardingData
import javax.inject.Inject

/**
 * Use case untuk menyimpan hasil onboarding:
 * 1. Apply tax/service defaults berdasar FnbType
 * 2. Save StoreSettings ke Room
 * 3. Seed default categories sesuai FnbType
 *
 * NOTE: Setelah AppDatabase.populateInitialData() dihapus,
 * use case ini jadi satu-satunya sumber kategori awal.
 */
class SaveOnboardingUseCase @Inject constructor(
    private val storeSettingsRepository: StoreRepository,
    private val categoryRepository: CategoryRepository,
) {

    suspend operator fun invoke(data: OnboardingData): Result<Unit> {
        val fnbType = data.fnbType
            ?: return Result.failure(IllegalStateException("FnbType wajib dipilih"))
        val serviceStyle = data.serviceStyle
            ?: return Result.failure(IllegalStateException("ServiceStyle wajib dipilih"))
        val customerCapacity = data.customerCapacity
            ?: return Result.failure(IllegalStateException("CustomerCapacity wajib dipilih"))

        return try {
            val taxDefaults = FnbDefaultsProvider.getTaxDefaults(fnbType)

            val settings = Store(
                storeName = data.storeName,
                storeAddress = data.storeAddress,
                storePhone = data.storePhone,
                storeLogoUri = data.storeLogoUri,
                fnbType = fnbType.name,
                serviceStyle = serviceStyle.name,
                customerCapacity = customerCapacity.name,
                openTime = data.openTime,
                closeTime = data.closeTime,
                taxEnabled = taxDefaults.taxEnabled,
                taxPercentage = taxDefaults.taxPercentage,
                serviceChargeEnabled = taxDefaults.serviceChargeEnabled,
                serviceChargePercentage = taxDefaults.serviceChargePercentage,
                priorityFeaturesCsv = data.priorityFeatures.joinToString(",") { it.name },
            )

            storeSettingsRepository.saveSettings(settings).getOrThrow()

            // Seed kategori — best-effort, tidak fail keseluruhan kalau error
            seedDefaultCategories(FnbDefaultsProvider.getDefaultCategories(fnbType))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Skip seed kalau user sudah punya kategori (idempotent).
     * Insert error per-kategori di-swallow agar 1 kategori gagal tidak abort semua.
     */
    private suspend fun seedDefaultCategories(defaults: List<Pair<String, String>>) {
        val existingCount = categoryRepository.getCategoryCount()
        if (existingCount > 0) return

        defaults.forEachIndexed { index, (name, emoji) ->
            categoryRepository.insertCategory(
                name = name,
                emoji = emoji,
                displayOrder = index,
            )
        }
    }
}
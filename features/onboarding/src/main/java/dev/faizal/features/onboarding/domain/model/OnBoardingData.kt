package dev.faizal.features.onboarding.domain.model

/**
 * Data yang dikumpulkan selama onboarding F&B.
 * Setelah register sukses, di-flush ke Firestore untuk personalisasi.
 */
data class OnboardingData(
    // Step 1: Jenis F&B
    val fnbType: FnbType? = null,

    // Step 2: Skala (jumlah pelanggan harian)
    val customerCapacity: CustomerCapacity? = null,

    // Step 3: Gaya layanan
    val serviceStyle: ServiceStyle? = null,

    // Step 4: Identitas warung/cafe
    val storeName: String = "",
    val storeAddress: String = "",
    val storePhone: String = "",
    val storeLogoUri: String? = null,

    // Step 5: Operasional
    val openTime: String = "08:00",
    val closeTime: String = "22:00",
    val taxEnabled: Boolean = false,
    val taxPercentage: Double = 0.0,
    val serviceChargeEnabled: Boolean = false,
    val serviceChargePercentage: Double = 0.0,

    // Step 6: Fitur prioritas (multi-select, max 3)
    val priorityFeatures: Set<FnbFeature> = emptySet(),

    // Skala lokasi (opsional di awal, default SOLO)
    val businessScale: BusinessScale = BusinessScale.SOLO,

    // Permission
    val notificationGranted: Boolean = false,
    val storageGranted: Boolean = false,
)
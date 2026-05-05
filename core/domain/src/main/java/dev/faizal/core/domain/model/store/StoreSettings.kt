package dev.faizal.core.domain.model.store

/**
 * Pengaturan toko milik UMKM yang menggunakan ZyPos.
 * Single-tenant: hanya 1 row di Room (id = 1).
 *
 * Data ini diisi saat onboarding dan dipakai untuk:
 * - Personalisasi header dashboard & PDF struk
 * - Konfigurasi dynamic tax & service charge
 * - Conditional UI (dine-in vs takeaway)
 * - Quick action di dashboard berdasar priority features
 */
data class Store(
    val id: Int = SINGLE_ROW_ID,
    val storeName: String,
    val storeAddress: String,
    val storePhone: String,
    val storeLogoUri: String?,

    // F&B Specifics
    val fnbType: String,                     // FnbType.name
    val serviceStyle: String,                // ServiceStyle.name
    val customerCapacity: String,            // CustomerCapacity.name

    // Operational
    val openTime: String,                    // "08:00"
    val closeTime: String,                   // "22:00"
    val taxEnabled: Boolean,
    val taxPercentage: Double,               // 10.0 = 10%
    val serviceChargeEnabled: Boolean,
    val serviceChargePercentage: Double,     // 5.0 = 5%

    // Priority features (CSV: "DINE_IN_TABLE,QUICK_ORDER,EXPORT_PDF")
    val priorityFeaturesCsv: String,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    /** Apakah service style ini punya konsep dine-in? */
    val supportsDineIn: Boolean
        get() = serviceStyle != "TAKEAWAY_ONLY"

    /** Parse CSV jadi list. */
    val priorityFeatures: List<String>
        get() = priorityFeaturesCsv.split(",").filter { it.isNotBlank() }

    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
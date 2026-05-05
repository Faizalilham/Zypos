package dev.faizal.features.onboarding.domain.usecase

import dev.faizal.features.onboarding.domain.model.FnbType


/**
 * Single source of truth untuk default values per FnbType.
 *
 * Aturan default:
 * - Restoran/Cafe/FastFood/Bakery/Catering → ada tax 10% (formal F&B biasanya kena PPN)
 * - Restoran → tambahan service charge 5% (umum di restoran)
 * - Warung/Beverage/Other → tax & service off (UMKM informal)
 */
object FnbDefaultsProvider {

    data class TaxDefaults(
        val taxEnabled: Boolean,
        val taxPercentage: Double,
        val serviceChargeEnabled: Boolean,
        val serviceChargePercentage: Double,
    )

    fun getTaxDefaults(fnbType: FnbType): TaxDefaults = when (fnbType) {
        FnbType.RESTAURANT -> TaxDefaults(
            taxEnabled = true,
            taxPercentage = 10.0,
            serviceChargeEnabled = true,
            serviceChargePercentage = 5.0,
        )
        FnbType.CAFE,
        FnbType.FAST_FOOD,
        FnbType.BAKERY,
        FnbType.CATERING -> TaxDefaults(
            taxEnabled = true,
            taxPercentage = 10.0,
            serviceChargeEnabled = false,
            serviceChargePercentage = 0.0,
        )
        FnbType.WARUNG,
        FnbType.BEVERAGE,
        FnbType.OTHER -> TaxDefaults(
            taxEnabled = false,
            taxPercentage = 0.0,
            serviceChargeEnabled = false,
            serviceChargePercentage = 0.0,
        )
    }

    /**
     * Default kategori menu per jenis F&B.
     * Pair(name, emoji).
     */
    fun getDefaultCategories(fnbType: FnbType): List<Pair<String, String>> = when (fnbType) {
        FnbType.CAFE -> listOf(
            "Coffee" to "☕",
            "Tea" to "🍵",
            "Non-Coffee" to "🥤",
            "Snack" to "🍪",
            "Dessert" to "🍰",
        )
        FnbType.RESTAURANT -> listOf(
            "Appetizer" to "🥗",
            "Main Course" to "🍽️",
            "Dessert" to "🍰",
            "Beverages" to "🥤",
        )
        FnbType.WARUNG -> listOf(
            "Nasi" to "🍚",
            "Lauk" to "🍖",
            "Sayur" to "🥬",
            "Minuman" to "🥤",
            "Snack" to "🍿",
        )
        FnbType.BAKERY -> listOf(
            "Roti" to "🍞",
            "Kue" to "🍰",
            "Pastry" to "🥐",
            "Beverages" to "☕",
        )
        FnbType.FAST_FOOD -> listOf(
            "Burger" to "🍔",
            "Chicken" to "🍗",
            "Sides" to "🍟",
            "Drinks" to "🥤",
        )
        FnbType.BEVERAGE -> listOf(
            "Coffee" to "☕",
            "Tea" to "🍵",
            "Smoothie" to "🥤",
            "Boba" to "🧋",
            "Snack" to "🍪",
        )
        FnbType.CATERING -> listOf(
            "Paket Hemat" to "🍱",
            "Paket Premium" to "🍽️",
            "A La Carte" to "🥗",
        )
        FnbType.OTHER -> listOf(
            "Menu Utama" to "🍽️",
            "Minuman" to "🥤",
            "Snack" to "🍪",
        )
    }
}
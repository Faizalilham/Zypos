package dev.faizal.features.onboarding.domain.model

/**
 * Sub-kategori jenis usaha F&B.
 * Hasil pilihan akan menentukan template menu, kategori default,
 * dan rekomendasi fitur di dashboard.
 */
enum class FnbType(
    val label: String,
    val emoji: String,
    val description: String,
) {
    CAFE("Cafe & Coffee Shop", "☕", "Kopi, minuman, light meals"),
    RESTAURANT("Restoran", "🍽️", "Full course, dine-in"),
    WARUNG("Warung Makan", "🍜", "Warteg, warung sederhana"),
    BAKERY("Bakery & Dessert", "🥐", "Roti, kue, dessert"),
    FAST_FOOD("Fast Food", "🍔", "Burger, fried chicken"),
    BEVERAGE("Kedai Minuman", "🧋", "Boba, juice, smoothie"),
    CATERING("Catering", "🍱", "Pesanan partai besar"),
    OTHER("Lainnya", "🍴", "Jenis F&B lainnya"),
}

/**
 * Skala bisnis berdasarkan jumlah pelanggan harian.
 * Membantu kami menyesuaikan performa & fitur yang ditampilkan.
 */
enum class CustomerCapacity(
    val label: String,
    val emoji: String,
    val description: String,
) {
    SMALL("Baru mulai", "🌱", "Kurang dari 30 pelanggan/hari"),
    MEDIUM("Sedang berkembang", "🚀", "30-100 pelanggan/hari"),
    BUSY("Sudah ramai", "🔥", "Lebih dari 100 pelanggan/hari"),
}

/**
 * Gaya layanan utama. Menentukan apakah fitur table management
 * & dine-in di-aktifkan secara default atau tidak.
 */
enum class ServiceStyle(
    val label: String,
    val emoji: String,
    val description: String,
) {
    DINE_IN_ONLY("Dine-in saja", "🪑", "Pelanggan makan di tempat"),
    TAKEAWAY_ONLY("Takeaway/delivery", "📦", "Pesanan dibawa pulang"),
    BOTH("Keduanya", "🔄", "Dine-in + takeaway"),
}

/**
 * Fitur F&B yang paling dibutuhkan user.
 * Pilihan ini menentukan layout dashboard dan quick action.
 */
enum class FnbFeature(
    val label: String,
    val emoji: String,
) {
    DINE_IN_TABLE("Manajemen meja & dine-in", "🪑"),
    SIZE_TEMPERATURE("Pilihan size & temperature", "🧊"),
    QUICK_ORDER("Order cepat saat ramai", "⚡"),
    SPLIT_BILL("Split bill antar pelanggan", "💰"),
    DAILY_RECAP("Tutup kasir harian", "📊"),
    EXPORT_PDF("Cetak struk PDF", "🧾"),
    MENU_FAVORITE("Menu favorit pelanggan", "⭐"),
    DISCOUNT("Diskon & promo", "💸"),
}

/**
 * Skala lokasi/operasional.
 */
enum class BusinessScale(
    val label: String,
    val emoji: String,
    val description: String,
) {
    SOLO("Solo", "👤", "Saya sendiri yang jadi kasir"),
    SMALL_TEAM("Tim kecil", "👥", "2-5 kasir, 1 lokasi"),
    MULTI_BRANCH("Multi-cabang", "🏢", "Lebih dari 1 lokasi"),
}
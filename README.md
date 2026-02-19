# ZyPos 🛒

ZyPos adalah aplikasi kasir (POS) Android yang dirancang untuk mempermudah pengelolaan transaksi penjualan. Aplikasi ini mendukung manajemen menu, keranjang belanja, riwayat transaksi, dan fitur favorit produk.

---

## ✨ Fitur

- **Dashboard** — Ringkasan aktivitas penjualan
- **Menu** — Manajemen daftar produk/menu
- **Order** — Pembuatan dan pengelolaan pesanan
- **Favorite** — Produk favorit untuk akses cepat
- **Transaction** — Riwayat dan detail transaksi
- **Export PDF** — Cetak struk transaksi dalam format PDF

---

## 🏗️ Arsitektur

Aplikasi ini menggunakan **Clean Architecture** dengan pola **MVVM (Model-View-ViewModel)** dan pendekatan **Multi-Module**.

```
ZyPos/
├── app/                          # Entry point aplikasi
│
├── build-logic/                  # Convention Plugins (konfigurasi Gradle terpusat)
│
├── core/
│   ├── common/                   # Utility & shared components
│   ├── data/                     # Repository, Room Database, data source
│   │   └── test/                 # Unit test repository & data source
│   ├── designsystem/             # Komponen UI & tema Material3
│   ├── domain/                   # Use case, model bisnis, interface repository
│   └── ui/                       # Shared UI components
│
└── features/
    ├── dashboard/                # Fitur dashboard
    │   └── test/                 # Unit test ViewModel dashboard
    ├── favorite/                 # Fitur favorit
    │   └── test/                 # Unit test ViewModel favorite
    ├── menu/                     # Fitur menu/produk
    │   └── test/                 # Unit test ViewModel menu
    ├── order/                    # Fitur pesanan
    │   └── test/                 # Unit test ViewModel order
    └── transaction/              # Fitur transaksi
        └── test/                 # Unit test ViewModel transaction
```

---

## 🛠️ Tech Stack

| Kategori | Teknologi |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| DI | Hilt |
| Database | Room |
| Navigation | Navigation Compose |
| Image Loading | Coil 3 |
| PDF | iText 7 |
| Async | Kotlin Coroutines + Flow |
| Build System | Gradle + Convention Plugins |

---

## 🧪 Testing

Setiap module memiliki unit test masing-masing yang terisolasi.

| Module | Cakupan Test |
|---|---|
| `core:data` | Repository, DAO, data source |
| `features:dashboard` | ViewModel |
| `features:favorite` | ViewModel |
| `features:menu` | ViewModel |
| `features:order` | ViewModel |
| `features:transaction` | ViewModel |

### Library Testing

| Library | Kegunaan |
|---|---|
| JUnit 4 | Framework testing utama |
| MockK | Mocking dependencies |
| Turbine | Testing Kotlin Flow |
| Truth | Assertion library |
| kotlinx-coroutines-test | Testing coroutines & Flow |
| androidx.arch.core:core-testing | InstantTaskExecutorRule untuk LiveData |

### Menjalankan Test

```bash
# Semua unit test
./gradlew test

# Unit test per module
./gradlew :core:data:test
./gradlew :features:order:test
./gradlew :features:menu:test

# Semua module sekaligus
./gradlew allTests
```

---

## ⚙️ Convention Plugins

Proyek ini menggunakan **Build Logic Convention Plugins** untuk mengurangi duplikasi konfigurasi Gradle antar module.

| Plugin ID | Kegunaan |
|---|---|
| `dev.faizal.android.application` | Konfigurasi base Android app |
| `dev.faizal.android.application.compose` | Tambah Compose ke app module |
| `dev.faizal.android.library` | Konfigurasi base Android library |
| `dev.faizal.android.compose` | Tambah Compose ke library module |
| `dev.faizal.android.hilt` | Konfigurasi Hilt + KSP |
| `dev.faizal.android.feature` | Bundle lengkap untuk feature module (library + hilt + compose + core dependencies) |
| `dev.faizal.kotlin.library` | Konfigurasi pure Kotlin/JVM module |

---

## 🚀 Cara Menjalankan

### Prasyarat

- Android Studio Hedgehog atau lebih baru
- JDK 11
- Android SDK minimum API 24

### Langkah-langkah

1. Clone repository ini:
   ```bash
   git clone https://github.com/faizal/zypos.git
   ```

2. Buka project di Android Studio

3. Sync Gradle:
   ```bash
   ./gradlew build
   ```

4. Jalankan di emulator atau perangkat fisik

---

## 📦 Persyaratan Sistem

- **Minimum SDK:** Android 7.0 (API 24)
- **Target SDK:** Android 15 (API 35)
- **Compile SDK:** 36

---

## Soon Publish Playstore
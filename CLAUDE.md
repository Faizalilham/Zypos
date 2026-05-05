# ZyPos — Panduan untuk Claude

## 🎯 Tentang Project
ZyPos adalah aplikasi kasir (POS) Android untuk manajemen transaksi penjualan, menu, pesanan, favorit, dan ekspor struk PDF.

---

## ⚠️ Aturan Sebelum Coding

Sebelum menulis kode, Claude **WAJIB** bertanya terlebih dahulu jika:
- Permintaan tidak menyebut lokasi file/module yang spesifik
- Ada lebih dari satu cara implementasi yang valid
- Menyangkut perubahan arsitektur atau alur navigasi
- Nama fitur/komponen belum jelas merujuk ke mana

> Jangan langsung coding. Ajukan pertanyaan klarifikasi terlebih dahulu, tunggu konfirmasi, baru mulai.

---

## 🏗️ Arsitektur — BACA DENGAN TELITI

Proyek ini menggunakan **Clean Architecture + MVVM + Multi-Module**. Setiap lapisan punya tanggung jawab yang ketat.

```
app/                        → Entry point, tidak berisi logika bisnis
build-logic/                → Convention Plugins Gradle (jangan ubah sembarangan)

core/
├── common/                 → Utility & helper yang dipakai bersama
├── data/                   → Repository impl, Room DAO, DataSource
├── designsystem/           → Komponen UI & tema Material3
├── domain/                 → Use case, model bisnis, interface repository
└── ui/                     → Shared UI components (bukan per-fitur)

features/
├── dashboard/
├── favorite/
├── menu/
├── order/
└── transaction/
```

### ❗ Aturan Multi-Module (PENTING)

- **Jangan** menaruh logika bisnis di `features/` — logika bisnis ada di `core/domain/`
- **Jangan** akses database langsung dari `features/` — harus lewat `core/data/`
- **Jangan** import antar `features/` secara langsung — gunakan navigasi
- Setiap fitur baru harus menggunakan plugin `dev.faizal.android.feature`
- Pure Kotlin module (tanpa Android) gunakan plugin `dev.faizal.kotlin.library`

---

## 🧭 Navigasi

Project menggunakan **Navigation Compose**. Alur navigasi didefinisikan di module `app/`, bukan di dalam masing-masing `features/`.

- Setiap feature module mengekspos **route** dan **composable screen**-nya
- Navigasi antar fitur dilakukan dari `app/`, bukan antar-feature langsung
- Jika ada permintaan navigasi baru, **tanya dulu** polanya sebelum implementasi

---

## 📝 Naming Convention

### File & Class
| Jenis | Format | Contoh |
|---|---|---|
| ViewModel | `[Fitur]ViewModel` | `MenuViewModel` |
| Use Case | `[Aksi][Entitas]UseCase` | `GetAllMenuUseCase` |
| Repository Interface | `[Entitas]Repository` | `MenuRepository` |
| Repository Impl | `[Entitas]RepositoryImpl` | `MenuRepositoryImpl` |
| DAO | `[Entitas]Dao` | `MenuDao` |
| Entity (Room) | `[Entitas]Entity` | `MenuEntity` |
| Screen Composable | `[Fitur]Screen` | `MenuScreen` |
| UI State | `[Fitur]UiState` | `MenuUiState` |

### Package
- Gunakan format: `dev.faizal.zypos.[module].[layer]`
- Contoh: `dev.faizal.zypos.menu.presentation`

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
| Build | Gradle + Convention Plugins |

---

## 🧪 Testing

Setiap module punya unit test masing-masing. Saat membuat fitur baru, **wajib** sertakan unit test.

### Library Testing
- **JUnit 4** — framework utama
- **MockK** — mocking
- **Turbine** — testing Flow
- **Truth** — assertion
- **kotlinx-coroutines-test** — testing coroutines

### Menjalankan Test
```bash
./gradlew test                        # semua test
./gradlew :core:data:test             # test core data
./gradlew :features:order:test        # test fitur order
./gradlew allTests                    # semua module
```

### Aturan Testing
- ViewModel test ada di `features/[fitur]/test/`
- Repository & DAO test ada di `core/data/test/`
- Gunakan `MockK` untuk mocking, bukan Mockito
- Gunakan `Turbine` untuk collect Flow di test

---

## ⚙️ Convention Plugins

| Plugin ID | Kapan Dipakai |
|---|---|
| `dev.faizal.android.application` | Module `app/` |
| `dev.faizal.android.application.compose` | Module `app/` dengan Compose |
| `dev.faizal.android.library` | Android library module |
| `dev.faizal.android.compose` | Library module yang pakai Compose |
| `dev.faizal.android.hilt` | Module yang pakai Hilt |
| `dev.faizal.android.feature` | Semua feature module |
| `dev.faizal.kotlin.library` | Pure Kotlin/JVM module |

---

## 🚀 Perintah Penting

```bash
./gradlew build         # build project
./gradlew test          # jalankan semua unit test
./gradlew allTests      # test semua module
```

---

## 📋 Checklist Sebelum Selesai

Sebelum menyatakan task selesai, Claude harus memastikan:
- [ ] Kode berada di module yang tepat sesuai arsitektur
- [ ] Tidak ada import langsung antar feature module
- [ ] Unit test sudah ditambahkan atau diupdate
- [ ] Naming convention sudah diikuti
- [ ] Tidak ada logika bisnis yang bocor ke layer UI
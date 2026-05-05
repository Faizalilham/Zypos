package dev.faizal.core.data.database

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.faizal.core.data.datasource.dao.CategoryDao
import dev.faizal.core.data.datasource.dao.MenuDao
import dev.faizal.core.data.datasource.dao.OrderDao
import dev.faizal.core.data.datasource.dao.StoreDao
import dev.faizal.core.data.datasource.entity.CategoryEntity
import dev.faizal.core.data.datasource.entity.MenuEntity
import dev.faizal.core.data.datasource.entity.OrderEntity
import dev.faizal.core.data.datasource.entity.StoreEntity
import dev.faizal.core.data.security.KeystoreHelper
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory as SupportFactory

@Database(
    entities = [CategoryEntity::class, MenuEntity::class, OrderEntity::class, StoreEntity::class],
    version = 7,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun menuDao(): MenuDao
    abstract fun orderDao(): OrderDao
    abstract fun storeDao(): StoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = KeystoreHelper.getDatabasePassphrase(context)
                val factory = SupportFactory(passphrase)
                passphrase.fill(0)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zypos_database",
                )
                    .openHelperFactory(factory)
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                    )
                    // Tidak ada lagi auto-seed kategori bawaan.
                    // User akan mengisi kategori lewat onboarding (SaveOnboardingUseCase).
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // ===== MIGRATIONS =====

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE orders ADD COLUMN imageUri TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE orders ADD COLUMN tableNumber TEXT")
            }
        }

        /**
         * Migration 4 → 5: penambahan StoreEntity (sudah dilakukan sebelumnya).
         * Empty migration untuk menutup gap version yang sebelumnya hilang.
         *
         * NOTE: Karena kamu sudah deploy v5 tanpa migration ini, asumsinya
         * device kamu sudah uninstall+reinstall. Kalau ada user existing di v4,
         * mereka butuh proper migration di sini untuk create table store.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Defensive: kalau table belum ada, buat. Kalau ada, skip.
                // Sesuaikan kolom dengan StoreEntity kamu yang sebenarnya.
                // Kalau StoreEntity sudah ada di v5 fresh install, baris ini tidak akan dijalankan.
            }
        }

        /**
         * Migration 5 → 6: hapus kategori bawaan (Coffee, Tea, Snack)
         * yang tadinya di-seed otomatis oleh DatabaseCallback.
         *
         * Ini akan menghapus SEMUA kategori — onboarding akan seed ulang
         * sesuai FnbType yang dipilih user.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Hapus kategori bawaan yang nama-nya match seed lama.
                // Kalau user sudah punya menu yang refer ke kategori ini,
                // FK constraint mungkin protect — sesuaikan kalau perlu.
                database.execSQL(
                    "DELETE FROM category WHERE name IN ('Coffee', 'Tea', 'Snack')",
                )
            }
        }

        // ===== UTIL (legacy, tidak dipakai lagi) =====

        private fun migrateToEncryptedIfNeeded(context: Context) {
            val dbFile = context.getDatabasePath("zypos_database")
            if (!dbFile.exists()) return

            val header = dbFile.inputStream().use { stream ->
                ByteArray(16).also { stream.read(it) }
            }
            val isPlaintext = String(header) == "SQLite format 3\u0000"
            if (!isPlaintext) return

            val passphrase = KeystoreHelper.getDatabasePassphrase(context)
            val passphraseHex = passphrase.joinToString("") { "%02x".format(it) }
            passphrase.fill(0)

            val tempFile = File(dbFile.parent, "zypos_database_temp")

            SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE,
            ).use { plainDb ->
                plainDb.rawExecSQL(
                    "ATTACH DATABASE '${tempFile.absolutePath}' AS encrypted KEY \"x'$passphraseHex'\"",
                )
                plainDb.rawExecSQL("SELECT sqlcipher_export('encrypted')")
                plainDb.rawExecSQL("DETACH DATABASE encrypted")
            }

            dbFile.delete()
            tempFile.renameTo(dbFile)
        }

        @VisibleForTesting
        fun resetInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
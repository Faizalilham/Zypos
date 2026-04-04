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
import dev.faizal.core.data.datasource.entity.CategoryEntity
import dev.faizal.core.data.datasource.entity.MenuEntity
import dev.faizal.core.data.datasource.entity.OrderEntity
import dev.faizal.core.data.security.KeystoreHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory as SupportFactory

@Database(
    entities = [CategoryEntity::class, MenuEntity::class, OrderEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    abstract fun menuDao(): MenuDao

    abstract fun orderDao(): OrderDao


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
                    "zypos_database"
                )
                    .openHelperFactory(factory)
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.categoryDao())
                    }
                }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE orders ADD COLUMN imageUri TEXT"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE orders ADD COLUMN tableNumber TEXT")
            }
        }

        private suspend fun populateInitialData(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                CategoryEntity(
                    name = "Coffee",
                    emoji = "☕",
                    displayOrder = 1,
                    isActive = true
                ),
                CategoryEntity(
                    name = "Tea",
                    emoji = "🍵",
                    displayOrder = 2,
                    isActive = true
                ),
                CategoryEntity(
                    name = "Snack",
                    emoji = "🍪",
                    displayOrder = 3,
                    isActive = true
                )
            )

            defaultCategories.forEach { category ->
                categoryDao.insertCategory(category)
            }
        }

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
                SQLiteDatabase.OPEN_READWRITE
            ).use { plainDb ->
                plainDb.rawExecSQL("ATTACH DATABASE '${tempFile.absolutePath}' AS encrypted KEY \"x'$passphraseHex'\"")
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
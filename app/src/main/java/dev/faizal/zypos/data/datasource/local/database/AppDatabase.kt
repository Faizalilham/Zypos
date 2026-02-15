package dev.faizal.zypos.data.datasource.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.faizal.zypos.data.datasource.local.dao.CategoryDao
import dev.faizal.zypos.data.datasource.local.dao.MenuDao
import dev.faizal.zypos.data.datasource.local.dao.OrderDao
import dev.faizal.zypos.data.datasource.local.entity.CategoryEntity
import dev.faizal.zypos.data.datasource.local.entity.MenuEntity
import dev.faizal.zypos.data.datasource.local.entity.OrderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CategoryEntity::class, MenuEntity::class, OrderEntity::class],
    version = 3,
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
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zypos_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
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
                // Add imageUri and imageResourceId columns to orders table
                database.execSQL(
                    "ALTER TABLE orders ADD COLUMN imageUri TEXT"
                )
            }
        }

        // Add default categories when database is created
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
    }
}
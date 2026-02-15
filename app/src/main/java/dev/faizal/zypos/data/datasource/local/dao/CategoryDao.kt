package dev.faizal.zypos.data.datasource.local.dao

import androidx.room.*
import dev.faizal.zypos.data.datasource.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY displayOrder ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY displayOrder ASC, name ASC")
    fun getActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)

    @Query("UPDATE categories SET isActive = :isActive WHERE id = :id")
    suspend fun updateCategoryStatus(id: Int, isActive: Boolean)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    // Check if category is used by any menu
    @Query("SELECT COUNT(*) FROM menu_items WHERE categoryId = :categoryId")
    suspend fun getMenuCountByCategory(categoryId: Int): Int

    // Get category usage statistics
    @Query("""
        SELECT c.*, COUNT(m.id) as menuCount 
        FROM categories c 
        LEFT JOIN menu_items m ON c.id = m.categoryId 
        GROUP BY c.id 
        ORDER BY c.displayOrder ASC
    """)
    fun getCategoriesWithMenuCount(): Flow<List<CategoryWithMenuCount>>
}

data class CategoryWithMenuCount(
    @Embedded val category: CategoryEntity,
    val menuCount: Int
)
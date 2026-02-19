package dev.faizal.core.domain.repository

import dev.faizal.core.domain.model.menu.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getAllCategories(): Flow<List<Category>>

    fun getActiveCategories(): Flow<List<Category>>

    suspend fun getCategoryById(id: Int): Category?

    suspend fun getCategoryByName(name: String): Category?

    suspend fun insertCategory(
        name: String,
        emoji: String,
        displayOrder: Int = 0
    ): Result<Long>

    suspend fun updateCategory(
        id: Int,
        name: String,
        emoji: String,
        displayOrder: Int,
        isActive: Boolean
    ): Result<Unit>

    suspend fun deleteCategory(id: Int): Result<Unit>

    suspend fun updateCategoryStatus(id: Int, isActive: Boolean): Result<Unit>

    suspend fun getCategoryCount(): Int

    suspend fun getMenuCountByCategory(categoryId: Int): Int
}




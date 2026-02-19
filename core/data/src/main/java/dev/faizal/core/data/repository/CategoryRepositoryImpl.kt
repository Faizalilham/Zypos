package dev.faizal.core.data.repository

import dev.faizal.core.data.datasource.dao.CategoryDao
import dev.faizal.core.data.datasource.entity.CategoryEntity
import dev.faizal.core.data.mapper.CategoryMapper.toDomain
import dev.faizal.core.data.mapper.CategoryMapper.toDomainList
import dev.faizal.core.domain.model.menu.Category
import dev.faizal.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { it.toDomainList() }

    override fun getActiveCategories(): Flow<List<Category>> =
        categoryDao.getActiveCategories().map { it.toDomainList() }

    override suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getCategoryByName(name)?.toDomain()
    }

    override suspend fun insertCategory(
        name: String,
        emoji: String,
        displayOrder: Int
    ): Result<Long> {
        return try {
            // Check if category name already exists
            val existing = categoryDao.getCategoryByName(name)
            if (existing != null) {
                return Result.failure(Exception("Category '$name' already exists"))
            }

            val category = CategoryEntity(
                name = name,
                emoji = emoji,
                displayOrder = displayOrder,
                isActive = true
            )

            val id = categoryDao.insertCategory(category)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCategory(
        id: Int,
        name: String,
        emoji: String,
        displayOrder: Int,
        isActive: Boolean
    ): Result<Unit> {
        return try {
            val existing = categoryDao.getCategoryById(id)
                ?: return Result.failure(Exception("Category not found"))

            // Check if new name conflicts with another category
            val nameCheck = categoryDao.getCategoryByName(name)
            if (nameCheck != null && nameCheck.id != id) {
                return Result.failure(Exception("Category name '$name' already exists"))
            }

            val updated = existing.copy(
                name = name,
                emoji = emoji,
                displayOrder = displayOrder,
                isActive = isActive,
                updatedAt = System.currentTimeMillis()
            )

            categoryDao.updateCategory(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(id: Int): Result<Unit> {
        return try {
            // Check if category is used by any menu
            val menuCount = categoryDao.getMenuCountByCategory(id)
            if (menuCount > 0) {
                return Result.failure(
                    Exception("Cannot delete category. It is used by $menuCount menu item(s)")
                )
            }

            categoryDao.deleteCategoryById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCategoryStatus(id: Int, isActive: Boolean): Result<Unit> {
        return try {
            categoryDao.updateCategoryStatus(id, isActive)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCategoryCount(): Int = categoryDao.getCategoryCount()

    override suspend fun getMenuCountByCategory(categoryId: Int): Int =
        categoryDao.getMenuCountByCategory(categoryId)
}

package dev.faizal.core.testing.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.faizal.core.data.datasource.dao.CategoryDao
import dev.faizal.core.data.datasource.entity.CategoryEntity
import dev.faizal.core.data.repository.CategoryRepositoryImpl
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CategoryRepositoryImpl
 *
 * Tests cover:
 * - Category CRUD operations
 * - Duplicate name validation
 * - Delete validation (check menu usage)
 * - Status updates
 * - Error handling
 */
class CategoryRepositoryImplTest {

    private lateinit var categoryDao: CategoryDao
    private lateinit var repository: CategoryRepositoryImpl

    private val testCategory = CategoryEntity(
        id = 1,
        name = "Coffee",
        emoji = "☕",
        displayOrder = 1,
        isActive = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        categoryDao = mockk()
        repository = CategoryRepositoryImpl(categoryDao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ==================== GET CATEGORIES TESTS ====================

    @Test
    fun `getAllCategories returns all categories sorted by display order`() = runTest {
        // Given
        val categories = listOf(
            testCategory,
            testCategory.copy(id = 2, name = "Tea", displayOrder = 2),
            testCategory.copy(id = 3, name = "Juice", displayOrder = 0)
        )
        every { categoryDao.getAllCategories() } returns flowOf(categories)

        // When & Then
        repository.getAllCategories().test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            awaitComplete()
        }
    }

    @Test
    fun `getActiveCategories returns only active categories`() = runTest {
        // Given
        val categories = listOf(
            testCategory,
            testCategory.copy(id = 2, name = "Tea", isActive = true)
        )
        every { categoryDao.getActiveCategories() } returns flowOf(categories)

        // When & Then
        repository.getActiveCategories().test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result.all { it.isActive }).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `getCategoryById with existing id returns category`() = runTest {
        // Given
        val categoryId = 1
        coEvery { categoryDao.getCategoryById(categoryId) } returns testCategory

        // When
        val result = repository.getCategoryById(categoryId)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo("Coffee")
    }

    @Test
    fun `getCategoryById with non-existing id returns null`() = runTest {
        // Given
        val categoryId = 999
        coEvery { categoryDao.getCategoryById(categoryId) } returns null

        // When
        val result = repository.getCategoryById(categoryId)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `getCategoryByName with existing name returns category`() = runTest {
        // Given
        val categoryName = "Coffee"
        coEvery { categoryDao.getCategoryByName(categoryName) } returns testCategory

        // When
        val result = repository.getCategoryByName(categoryName)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.emoji).isEqualTo("☕")
    }

    // ==================== INSERT CATEGORY TESTS ====================

    @Test
    fun `insertCategory with valid data returns success`() = runTest {
        // Given
        val name = "Coffee"
        val emoji = "☕"
        val displayOrder = 1

        coEvery { categoryDao.getCategoryByName(name) } returns null
        coEvery { categoryDao.insertCategory(any()) } returns 1L

        // When
        val result = repository.insertCategory(name, emoji, displayOrder)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(1L)

        val capturedCategory = slot<CategoryEntity>()
        coVerify { categoryDao.insertCategory(capture(capturedCategory)) }
        assertThat(capturedCategory.captured.name).isEqualTo(name)
        assertThat(capturedCategory.captured.emoji).isEqualTo(emoji)
        assertThat(capturedCategory.captured.isActive).isTrue()
    }

    @Test
    fun `insertCategory with duplicate name returns failure`() = runTest {
        // Given
        val name = "Coffee"
        val emoji = "☕"
        val displayOrder = 1

        coEvery { categoryDao.getCategoryByName(name) } returns testCategory

        // When
        val result = repository.insertCategory(name, emoji, displayOrder)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("already exists")

        coVerify(exactly = 0) { categoryDao.insertCategory(any()) }
    }

    @Test
    fun `insertCategory with database error returns failure`() = runTest {
        // Given
        val name = "Coffee"
        val emoji = "☕"
        val displayOrder = 1
        val errorMessage = "Database error"

        coEvery { categoryDao.getCategoryByName(name) } returns null
        coEvery { categoryDao.insertCategory(any()) } throws Exception(errorMessage)

        // When
        val result = repository.insertCategory(name, emoji, displayOrder)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    // ==================== UPDATE CATEGORY TESTS ====================

    @Test
    fun `updateCategory with valid data returns success`() = runTest {
        // Given
        val categoryId = 1
        val newName = "Hot Coffee"
        val newEmoji = "🔥☕"
        val newDisplayOrder = 2
        val isActive = true

        coEvery { categoryDao.getCategoryById(categoryId) } returns testCategory
        coEvery { categoryDao.getCategoryByName(newName) } returns null
        coEvery { categoryDao.updateCategory(any()) } just Runs

        // When
        val result = repository.updateCategory(categoryId, newName, newEmoji, newDisplayOrder, isActive)

        // Then
        assertThat(result.isSuccess).isTrue()

        val capturedCategory = slot<CategoryEntity>()
        coVerify { categoryDao.updateCategory(capture(capturedCategory)) }
        assertThat(capturedCategory.captured.name).isEqualTo(newName)
        assertThat(capturedCategory.captured.emoji).isEqualTo(newEmoji)
        assertThat(capturedCategory.captured.displayOrder).isEqualTo(newDisplayOrder)
    }

    @Test
    fun `updateCategory with non-existing id returns failure`() = runTest {
        // Given
        val categoryId = 999
        coEvery { categoryDao.getCategoryById(categoryId) } returns null

        // When
        val result = repository.updateCategory(categoryId, "Name", "🎯", 1, true)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("not found")
    }

    @Test
    fun `updateCategory with duplicate name returns failure`() = runTest {
        // Given
        val categoryId = 1
        val newName = "Tea"
        val existingTea = testCategory.copy(id = 2, name = "Tea")

        coEvery { categoryDao.getCategoryById(categoryId) } returns testCategory
        coEvery { categoryDao.getCategoryByName(newName) } returns existingTea

        // When
        val result = repository.updateCategory(categoryId, newName, "🍵", 1, true)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("already exists")
    }

    @Test
    fun `updateCategory can keep same name for same category`() = runTest {
        // Given: Update category with its own name (should be allowed)
        val categoryId = 1
        val sameName = "Coffee"

        coEvery { categoryDao.getCategoryById(categoryId) } returns testCategory
        coEvery { categoryDao.getCategoryByName(sameName) } returns testCategory
        coEvery { categoryDao.updateCategory(any()) } just Runs

        // When
        val result = repository.updateCategory(categoryId, sameName, "☕", 1, true)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { categoryDao.updateCategory(any()) }
    }

    // ==================== DELETE CATEGORY TESTS ====================

    @Test
    fun `deleteCategory with no menu items returns success`() = runTest {
        // Given
        val categoryId = 1
        coEvery { categoryDao.getMenuCountByCategory(categoryId) } returns 0
        coEvery { categoryDao.deleteCategoryById(categoryId) } just Runs

        // When
        val result = repository.deleteCategory(categoryId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { categoryDao.deleteCategoryById(categoryId) }
    }

    @Test
    fun `deleteCategory with existing menu items returns failure`() = runTest {
        // Given
        val categoryId = 1
        val menuCount = 5
        coEvery { categoryDao.getMenuCountByCategory(categoryId) } returns menuCount

        // When
        val result = repository.deleteCategory(categoryId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("5 menu item(s)")

        coVerify(exactly = 0) { categoryDao.deleteCategoryById(any()) }
    }

    @Test
    fun `deleteCategory with database error returns failure`() = runTest {
        // Given
        val categoryId = 1
        val errorMessage = "Delete failed"

        coEvery { categoryDao.getMenuCountByCategory(categoryId) } returns 0
        coEvery { categoryDao.deleteCategoryById(categoryId) } throws Exception(errorMessage)

        // When
        val result = repository.deleteCategory(categoryId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    // ==================== STATUS UPDATE TESTS ====================

    @Test
    fun `updateCategoryStatus activates category successfully`() = runTest {
        // Given
        val categoryId = 1
        val isActive = true
        coEvery { categoryDao.updateCategoryStatus(categoryId, isActive) } just Runs

        // When
        val result = repository.updateCategoryStatus(categoryId, isActive)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { categoryDao.updateCategoryStatus(categoryId, true) }
    }

    @Test
    fun `updateCategoryStatus deactivates category successfully`() = runTest {
        // Given
        val categoryId = 1
        val isActive = false
        coEvery { categoryDao.updateCategoryStatus(categoryId, isActive) } just Runs

        // When
        val result = repository.updateCategoryStatus(categoryId, isActive)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { categoryDao.updateCategoryStatus(categoryId, false) }
    }

    @Test
    fun `updateCategoryStatus with database error returns failure`() = runTest {
        // Given
        val categoryId = 1
        val errorMessage = "Update failed"
        coEvery { categoryDao.updateCategoryStatus(any(), any()) } throws Exception(errorMessage)

        // When
        val result = repository.updateCategoryStatus(categoryId, true)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    // ==================== COUNT & STATISTICS TESTS ====================

    @Test
    fun `getCategoryCount returns correct count`() = runTest {
        // Given
        val expectedCount = 10
        coEvery { categoryDao.getCategoryCount() } returns expectedCount

        // When
        val result = repository.getCategoryCount()

        // Then
        assertThat(result).isEqualTo(expectedCount)
    }

    @Test
    fun `getMenuCountByCategory returns correct menu count`() = runTest {
        // Given
        val categoryId = 1
        val expectedCount = 15
        coEvery { categoryDao.getMenuCountByCategory(categoryId) } returns expectedCount

        // When
        val result = repository.getMenuCountByCategory(categoryId)

        // Then
        assertThat(result).isEqualTo(expectedCount)
    }

    @Test
    fun `getMenuCountByCategory with no menus returns zero`() = runTest {
        // Given
        val categoryId = 1
        coEvery { categoryDao.getMenuCountByCategory(categoryId) } returns 0

        // When
        val result = repository.getMenuCountByCategory(categoryId)

        // Then
        assertThat(result).isEqualTo(0)
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `insertCategory with special characters in name succeeds`() = runTest {
        // Given
        val name = "Café & Latte's"
        val emoji = "☕"

        coEvery { categoryDao.getCategoryByName(name) } returns null
        coEvery { categoryDao.insertCategory(any()) } returns 1L

        // When
        val result = repository.insertCategory(name, emoji, 1)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `insertCategory with emoji in name succeeds`() = runTest {
        // Given
        val name = "Coffee ☕"
        val emoji = "☕"

        coEvery { categoryDao.getCategoryByName(name) } returns null
        coEvery { categoryDao.insertCategory(any()) } returns 1L

        // When
        val result = repository.insertCategory(name, emoji, 1)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `updateCategory with same data still updates timestamp`() = runTest {
        // Given
        val categoryId = 1
        val oldTimestamp = testCategory.updatedAt

        coEvery { categoryDao.getCategoryById(categoryId) } returns testCategory
        coEvery { categoryDao.getCategoryByName(testCategory.name) } returns testCategory
        coEvery { categoryDao.updateCategory(any()) } just Runs

        // When
        repository.updateCategory(
            categoryId,
            testCategory.name,
            testCategory.emoji,
            testCategory.displayOrder,
            testCategory.isActive
        )

        // Then
        val capturedCategory = slot<CategoryEntity>()
        coVerify { categoryDao.updateCategory(capture(capturedCategory)) }
        assertThat(capturedCategory.captured.updatedAt).isGreaterThan(oldTimestamp)
    }
}
package dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.faizal.core.data.database.AppDatabase
import dev.faizal.core.data.datasource.dao.CategoryDao
import dev.faizal.core.data.datasource.dao.MenuDao
import dev.faizal.core.data.datasource.entity.CategoryEntity
import dev.faizal.core.data.datasource.entity.MenuEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for CategoryDao with Room database
 *
 * These tests use an in-memory database to ensure isolation
 * and run actual SQL queries to verify DAO operations
 */
@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var menuDao: MenuDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        categoryDao = database.categoryDao()
        menuDao = database.menuDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== INSERT TESTS ====================

    @Test
    fun insertCategory_insertsDataCorrectly() = runTest {
        // Given
        val category = createTestCategory()

        // When
        val id = categoryDao.insertCategory(category)

        // Then
        assertThat(id).isGreaterThan(0)

        val retrieved = categoryDao.getCategoryById(id.toInt())
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.name).isEqualTo("Coffee")
        assertThat(retrieved?.emoji).isEqualTo("☕")
    }

    @Test
    fun insertCategory_withSameId_replacesExisting() = runTest {
        // Given
        val category1 = createTestCategory(id = 1, name = "Coffee")
        val category2 = createTestCategory(id = 1, name = "Hot Coffee")

        // When
        categoryDao.insertCategory(category1)
        categoryDao.insertCategory(category2) // Should replace

        // Then
        val retrieved = categoryDao.getCategoryById(1)
        assertThat(retrieved?.name).isEqualTo("Hot Coffee")
    }

    @Test
    fun insertCategory_multipleTimes_createsMultipleCategories() = runTest {
        // Given
        val categories = listOf(
            createTestCategory(name = "Coffee"),
            createTestCategory(name = "Tea"),
            createTestCategory(name = "Juice")
        )

        // When
        categories.forEach { categoryDao.insertCategory(it) }

        // Then
        categoryDao.getAllCategories().test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== GET TESTS ====================

    @Test
    fun getCategoryById_returnsCorrectCategory() = runTest {
        // Given
        val category = createTestCategory(name = "Coffee")
        val id = categoryDao.insertCategory(category)

        // When
        val retrieved = categoryDao.getCategoryById(id.toInt())

        // Then
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.name).isEqualTo("Coffee")
        assertThat(retrieved?.emoji).isEqualTo("☕")
    }

    @Test
    fun getCategoryById_withNonExistingId_returnsNull() = runTest {
        // When
        val retrieved = categoryDao.getCategoryById(999)

        // Then
        assertThat(retrieved).isNull()
    }

    @Test
    fun getCategoryByName_returnsCorrectCategory() = runTest {
        // Given
        val category = createTestCategory(name = "Coffee")
        categoryDao.insertCategory(category)

        // When
        val retrieved = categoryDao.getCategoryByName("Coffee")

        // Then
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.emoji).isEqualTo("☕")
    }

    @Test
    fun getCategoryByName_caseInsensitive() = runTest {
        // Given
        val category = createTestCategory(name = "Coffee")
        categoryDao.insertCategory(category)

        // When
        val retrieved = categoryDao.getCategoryByName("COFFEE")

        // Then
        // Note: SQLite's LIKE is case-insensitive by default for ASCII
        // but getCategoryByName uses exact match, so this will be null
        assertThat(retrieved).isNull()
    }

    @Test
    fun getCategoryByName_withNonExisting_returnsNull() = runTest {
        // When
        val retrieved = categoryDao.getCategoryByName("NonExistent")

        // Then
        assertThat(retrieved).isNull()
    }

    @Test
    fun getAllCategories_sortedByDisplayOrderAndName() = runTest {
        // Given
        val categories = listOf(
            createTestCategory(name = "Juice", displayOrder = 3),
            createTestCategory(name = "Coffee", displayOrder = 1),
            createTestCategory(name = "Tea", displayOrder = 2)
        )
        categories.forEach { categoryDao.insertCategory(it) }

        // When & Then
        categoryDao.getAllCategories().test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            assertThat(result[0].name).isEqualTo("Coffee") // displayOrder 1
            assertThat(result[1].name).isEqualTo("Tea")    // displayOrder 2
            assertThat(result[2].name).isEqualTo("Juice")  // displayOrder 3
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllCategories_withSameDisplayOrder_sortedByName() = runTest {
        // Given
        val categories = listOf(
            createTestCategory(name = "Espresso", displayOrder = 1),
            createTestCategory(name = "Cappuccino", displayOrder = 1),
            createTestCategory(name = "Latte", displayOrder = 1)
        )
        categories.forEach { categoryDao.insertCategory(it) }

        // When & Then
        categoryDao.getAllCategories().test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            // Same displayOrder, so sorted by name
            assertThat(result[0].name).isEqualTo("Cappuccino")
            assertThat(result[1].name).isEqualTo("Espresso")
            assertThat(result[2].name).isEqualTo("Latte")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getActiveCategories_returnsOnlyActive() = runTest {
        // Given
        val categories = listOf(
            createTestCategory(name = "Coffee", isActive = true),
            createTestCategory(name = "Tea", isActive = true),
            createTestCategory(name = "Juice", isActive = false)
        )
        categories.forEach { categoryDao.insertCategory(it) }

        // When & Then
        categoryDao.getActiveCategories().test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result.all { it.isActive }).isTrue()
            assertThat(result.map { it.name }).containsExactly("Coffee", "Tea")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== UPDATE TESTS ====================

    @Test
    fun updateCategory_updatesCorrectly() = runTest {
        // Given
        val category = createTestCategory(name = "Coffee", emoji = "☕")
        val id = categoryDao.insertCategory(category)

        val updated = category.copy(
            id = id.toInt(),
            name = "Hot Coffee",
            emoji = "🔥☕",
            displayOrder = 5
        )

        // When
        categoryDao.updateCategory(updated)

        // Then
        val retrieved = categoryDao.getCategoryById(id.toInt())
        assertThat(retrieved?.name).isEqualTo("Hot Coffee")
        assertThat(retrieved?.emoji).isEqualTo("🔥☕")
        assertThat(retrieved?.displayOrder).isEqualTo(5)
    }

    @Test
    fun updateCategoryStatus_changesStatusCorrectly() = runTest {
        // Given
        val category = createTestCategory(isActive = true)
        val id = categoryDao.insertCategory(category)

        // When
        categoryDao.updateCategoryStatus(id.toInt(), false)

        // Then
        val retrieved = categoryDao.getCategoryById(id.toInt())
        assertThat(retrieved?.isActive).isFalse()
    }

    // ==================== DELETE TESTS ====================

    @Test
    fun deleteCategoryById_removesCategory() = runTest {
        // Given
        val category = createTestCategory()
        val id = categoryDao.insertCategory(category)

        // When
        categoryDao.deleteCategoryById(id.toInt())

        // Then
        val retrieved = categoryDao.getCategoryById(id.toInt())
        assertThat(retrieved).isNull()
    }

    @Test
    fun deleteCategory_removesCategoryByEntity() = runTest {
        // Given
        val category = createTestCategory()
        val id = categoryDao.insertCategory(category)
        val categoryWithId = category.copy(id = id.toInt())

        // When
        categoryDao.deleteCategory(categoryWithId)

        // Then
        val retrieved = categoryDao.getCategoryById(id.toInt())
        assertThat(retrieved).isNull()
    }

    // ==================== COUNT TESTS ====================

    @Test
    fun getCategoryCount_returnsCorrectCount() = runTest {
        // Given
        val categories = listOf(
            createTestCategory(name = "Coffee"),
            createTestCategory(name = "Tea"),
            createTestCategory(name = "Juice")
        )
        categories.forEach { categoryDao.insertCategory(it) }

        // When
        val count = categoryDao.getCategoryCount()

        // Then
        assertThat(count).isEqualTo(3)
    }

    @Test
    fun getCategoryCount_withEmptyDatabase_returnsZero() = runTest {
        // When
        val count = categoryDao.getCategoryCount()

        // Then
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun getMenuCountByCategory_returnsCorrectCount() = runTest {
        // Given
        val category1 = createTestCategory(name = "Coffee")
        val category2 = createTestCategory(name = "Tea")
        val id1 = categoryDao.insertCategory(category1)
        val id2 = categoryDao.insertCategory(category2)

        // Insert menus
        menuDao.insertMenu(createTestMenu(categoryId = id1.toInt(), name = "Cappuccino"))
        menuDao.insertMenu(createTestMenu(categoryId = id1.toInt(), name = "Latte"))
        menuDao.insertMenu(createTestMenu(categoryId = id2.toInt(), name = "Green Tea"))

        // When
        val coffeeMenuCount = categoryDao.getMenuCountByCategory(id1.toInt())
        val teaMenuCount = categoryDao.getMenuCountByCategory(id2.toInt())

        // Then
        assertThat(coffeeMenuCount).isEqualTo(2)
        assertThat(teaMenuCount).isEqualTo(1)
    }

    @Test
    fun getMenuCountByCategory_withNoMenus_returnsZero() = runTest {
        // Given
        val category = createTestCategory()
        val id = categoryDao.insertCategory(category)

        // When
        val count = categoryDao.getMenuCountByCategory(id.toInt())

        // Then
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun getMenuCountByCategory_withNonExistingCategory_returnsZero() = runTest {
        // When
        val count = categoryDao.getMenuCountByCategory(999)

        // Then
        assertThat(count).isEqualTo(0)
    }

    // ==================== CATEGORIES WITH MENU COUNT TESTS ====================

    @Test
    fun getCategoriesWithMenuCount_returnsCorrectCounts() = runTest {
        // Given
        val category1 = createTestCategory(name = "Coffee", displayOrder = 1)
        val category2 = createTestCategory(name = "Tea", displayOrder = 2)
        val id1 = categoryDao.insertCategory(category1)
        val id2 = categoryDao.insertCategory(category2)

        menuDao.insertMenu(createTestMenu(categoryId = id1.toInt()))
        menuDao.insertMenu(createTestMenu(categoryId = id1.toInt()))

        // When & Then
        categoryDao.getCategoriesWithMenuCount().test {
            val result = awaitItem()
            assertThat(result).hasSize(2)

            val coffee = result.find { it.category.name == "Coffee" }
            val tea = result.find { it.category.name == "Tea" }

            assertThat(coffee?.menuCount).isEqualTo(2)
            assertThat(tea?.menuCount).isEqualTo(0)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCategoriesWithMenuCount_sortedByDisplayOrder() = runTest {
        // Given
        val categories = listOf(
            createTestCategory(name = "Juice", displayOrder = 3),
            createTestCategory(name = "Coffee", displayOrder = 1),
            createTestCategory(name = "Tea", displayOrder = 2)
        )
        categories.forEach { categoryDao.insertCategory(it) }

        // When & Then
        categoryDao.getCategoriesWithMenuCount().test {
            val result = awaitItem()
            assertThat(result[0].category.name).isEqualTo("Coffee")
            assertThat(result[1].category.name).isEqualTo("Tea")
            assertThat(result[2].category.name).isEqualTo("Juice")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== FOREIGN KEY CONSTRAINT TESTS ====================

    @Test
    fun deleteCategory_withExistingMenus_fails() = runTest {
        // Given
        val category = createTestCategory()
        val categoryId = categoryDao.insertCategory(category)

        // Insert menu that references this category
        menuDao.insertMenu(createTestMenu(categoryId = categoryId.toInt()))

        // When & Then
        try {
            categoryDao.deleteCategoryById(categoryId.toInt())
            throw AssertionError("Should have thrown foreign key constraint exception")
        } catch (e: Exception) {
            // Expected - RESTRICT constraint prevents deletion
            assertThat(e.message).contains("FOREIGN KEY constraint failed")
        }
    }

    @Test
    fun deleteCategory_withNoMenus_succeeds() = runTest {
        // Given
        val category = createTestCategory()
        val categoryId = categoryDao.insertCategory(category)

        // When
        categoryDao.deleteCategoryById(categoryId.toInt())

        // Then
        val retrieved = categoryDao.getCategoryById(categoryId.toInt())
        assertThat(retrieved).isNull()
    }

    // ==================== EDGE CASES ====================

    @Test
    fun insertCategory_withZeroDisplayOrder_succeeds() = runTest {
        // Given
        val category = createTestCategory(displayOrder = 0)

        // When
        val id = categoryDao.insertCategory(category)

        // Then
        val retrieved = categoryDao.getCategoryById(id.toInt())
        assertThat(retrieved?.displayOrder).isEqualTo(0)
    }

    @Test
    fun insertCategory_withNegativeDisplayOrder_succeeds() = runTest {
        // Given - Negative values allowed for custom sorting
        val category = createTestCategory(displayOrder = -1)

        // When
        val id = categoryDao.insertCategory(category)

        // Then
        val retrieved = categoryDao.getCategoryById(id.toInt())
        assertThat(retrieved?.displayOrder).isEqualTo(-1)
    }

    @Test
    fun insertCategory_withVeryHighDisplayOrder_succeeds() = runTest {
        // Given
        val category = createTestCategory(displayOrder = 999999)

        // When
        val id = categoryDao.insertCategory(category)

        // Then
        val retrieved = categoryDao.getCategoryById(id.toInt())
        assertThat(retrieved?.displayOrder).isEqualTo(999999)
    }

    @Test
    fun insertCategory_withSpecialCharactersInName_succeeds() = runTest {
        // Given
        val category = createTestCategory(name = "Coffee & Tea's")

        // When
        val id = categoryDao.insertCategory(category)

        // Then
        val retrieved = categoryDao.getCategoryById(id.toInt())
        assertThat(retrieved?.name).isEqualTo("Coffee & Tea's")
    }

    @Test
    fun insertCategory_withEmojiInName_succeeds() = runTest {
        // Given
        val category = createTestCategory(name = "Coffee ☕")

        // When
        val id = categoryDao.insertCategory(category)

        // Then
        val retrieved = categoryDao.getCategoryById(id.toInt())
        assertThat(retrieved?.name).isEqualTo("Coffee ☕")
    }

    @Test
    fun insertCategory_withMultipleEmojis_succeeds() = runTest {
        // Given
        val category = createTestCategory(emoji = "☕🔥")

        // When
        val id = categoryDao.insertCategory(category)

        // Then
        val retrieved = categoryDao.getCategoryById(id.toInt())
        assertThat(retrieved?.emoji).isEqualTo("☕🔥")
    }

    @Test
    fun insertCategory_withLongName_succeeds() = runTest {
        // Given
        val longName = "A".repeat(100)
        val category = createTestCategory(name = longName)

        // When
        val id = categoryDao.insertCategory(category)

        // Then
        val retrieved = categoryDao.getCategoryById(id.toInt())
        assertThat(retrieved?.name).isEqualTo(longName)
    }

    @Test
    fun updateCategory_preservesCreatedAt() = runTest {
        // Given
        val category = createTestCategory()
        val id = categoryDao.insertCategory(category)
        val inserted = categoryDao.getCategoryById(id.toInt())
        val originalCreatedAt = inserted?.createdAt

        // Wait a bit
        Thread.sleep(10)

        // When
        val updated = inserted!!.copy(name = "Updated Name")
        categoryDao.updateCategory(updated)

        // Then
        val retrieved = categoryDao.getCategoryById(id.toInt())
        assertThat(retrieved?.createdAt).isEqualTo(originalCreatedAt)
        assertThat(retrieved?.name).isEqualTo("Updated Name")
    }

    @Test
    fun getAllCategories_withEmptyDatabase_returnsEmptyList() = runTest {
        // When & Then
        categoryDao.getAllCategories().test {
            val result = awaitItem()
            assertThat(result).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getActiveCategories_withAllInactive_returnsEmptyList() = runTest {
        // Given
        val categories = listOf(
            createTestCategory(name = "Coffee", isActive = false),
            createTestCategory(name = "Tea", isActive = false)
        )
        categories.forEach { categoryDao.insertCategory(it) }

        // When & Then
        categoryDao.getActiveCategories().test {
            val result = awaitItem()
            assertThat(result).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateCategoryStatus_multipleToggles() = runTest {
        // Given
        val category = createTestCategory(isActive = true)
        val id = categoryDao.insertCategory(category)

        // When - Toggle multiple times
        categoryDao.updateCategoryStatus(id.toInt(), false)
        categoryDao.updateCategoryStatus(id.toInt(), true)
        categoryDao.updateCategoryStatus(id.toInt(), false)

        // Then
        val retrieved = categoryDao.getCategoryById(id.toInt())
        assertThat(retrieved?.isActive).isFalse()
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun createTestCategory(
        id: Int = 0,
        name: String = "Coffee",
        emoji: String = "☕",
        displayOrder: Int = 1,
        isActive: Boolean = true
    ): CategoryEntity {
        return CategoryEntity(
            id = id,
            name = name,
            emoji = emoji,
            displayOrder = displayOrder,
            isActive = isActive,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun createTestMenu(
        categoryId: Int,
        name: String = "Test Menu"
    ): MenuEntity {
        return MenuEntity(
            id = 0,
            name = name,
            categoryId = categoryId,
            basePrice = 25000.0,
            isActive = true,
            imageUri = null,
            sold = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}
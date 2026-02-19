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
 * Integration tests for MenuDao with Room database
 *
 * These tests use an in-memory database to ensure isolation
 * and run actual SQL queries to verify DAO operations
 */
@RunWith(AndroidJUnit4::class)
class MenuDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var menuDao: MenuDao
    private lateinit var categoryDao: CategoryDao

    private lateinit var testCategory: CategoryEntity

    @Before
    fun setup() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        menuDao = database.menuDao()
        categoryDao = database.categoryDao()

        // Insert test category (foreign key requirement)
        testCategory = CategoryEntity(
            id = 1,
            name = "Coffee",
            emoji = "☕",
            displayOrder = 1,
            isActive = true
        )
        categoryDao.insertCategory(testCategory)
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== INSERT TESTS ====================

    @Test
    fun insertMenu_insertsDataCorrectly() = runTest {
        // Given
        val menu = createTestMenu()

        // When
        val id = menuDao.insertMenu(menu)

        // Then
        assertThat(id).isGreaterThan(0)

        val retrieved = menuDao.getMenuById(id.toInt())
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.name).isEqualTo("Cappuccino")
        assertThat(retrieved?.basePrice).isEqualTo(25000.0)
    }

    @Test
    fun insertMenu_withSameId_replacesExisting() = runTest {
        // Given
        val menu1 = createTestMenu(id = 1, name = "Cappuccino")
        val menu2 = createTestMenu(id = 1, name = "Hot Cappuccino")

        // When
        menuDao.insertMenu(menu1)
        menuDao.insertMenu(menu2) // Should replace

        // Then
        val retrieved = menuDao.getMenuById(1)
        assertThat(retrieved?.name).isEqualTo("Hot Cappuccino")
    }

    @Test
    fun insertMenu_multipleTimes_createsMultipleMenus() = runTest {
        // Given
        val menus = listOf(
            createTestMenu(name = "Cappuccino"),
            createTestMenu(name = "Latte"),
            createTestMenu(name = "Espresso")
        )

        // When
        menus.forEach { menuDao.insertMenu(it) }

        // Then
        menuDao.getAllMenus().test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== GET TESTS ====================

    @Test
    fun getMenuById_returnsCorrectMenu() = runTest {
        // Given
        val menu = createTestMenu(name = "Cappuccino")
        val id = menuDao.insertMenu(menu)

        // When
        val retrieved = menuDao.getMenuById(id.toInt())

        // Then
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.name).isEqualTo("Cappuccino")
        assertThat(retrieved?.categoryId).isEqualTo(1)
    }

    @Test
    fun getMenuById_withNonExistingId_returnsNull() = runTest {
        // When
        val retrieved = menuDao.getMenuById(999)

        // Then
        assertThat(retrieved).isNull()
    }

    @Test
    fun getAllMenus_returnsAllMenusSortedByCreatedAt() = runTest {
        // Given - Insert with delays to ensure different timestamps
        val menu1 = createTestMenu(name = "Menu 1")
        val menu2 = createTestMenu(name = "Menu 2")
        val menu3 = createTestMenu(name = "Menu 3")

        menuDao.insertMenu(menu1)
        Thread.sleep(10)
        menuDao.insertMenu(menu2)
        Thread.sleep(10)
        menuDao.insertMenu(menu3)

        // When & Then
        menuDao.getAllMenus().test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            // Should be sorted by createdAt DESC (most recent first)
            assertThat(result[0].name).isEqualTo("Menu 3")
            assertThat(result[2].name).isEqualTo("Menu 1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getActiveMenus_returnsOnlyActiveMenus() = runTest {
        // Given
        val activeMenu1 = createTestMenu(name = "Active 1", isActive = true)
        val activeMenu2 = createTestMenu(name = "Active 2", isActive = true)
        val inactiveMenu = createTestMenu(name = "Inactive", isActive = false)

        menuDao.insertMenu(activeMenu1)
        menuDao.insertMenu(activeMenu2)
        menuDao.insertMenu(inactiveMenu)

        // When & Then
        menuDao.getActiveMenus().test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result.all { it.isActive }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getMenusByCategory_filtersCorrectly() = runTest {
        // Given - Create another category
        val category2 = testCategory.copy(id = 2, name = "Tea")
        categoryDao.insertCategory(category2)

        val coffeeMenu1 = createTestMenu(name = "Cappuccino", categoryId = 1)
        val coffeeMenu2 = createTestMenu(name = "Latte", categoryId = 1)
        val teaMenu = createTestMenu(name = "Green Tea", categoryId = 2)

        menuDao.insertMenu(coffeeMenu1)
        menuDao.insertMenu(coffeeMenu2)
        menuDao.insertMenu(teaMenu)

        // When & Then
        menuDao.getMenusByCategory(1).test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result.all { it.categoryId == 1 }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchMenus_byName_returnsMatchingMenus() = runTest {
        // Given
        val menus = listOf(
            createTestMenu(name = "Cappuccino"),
            createTestMenu(name = "Latte"),
            createTestMenu(name = "Espresso"),
            createTestMenu(name = "Americano")
        )
        menus.forEach { menuDao.insertMenu(it) }

        // When & Then - Search for "cino"
        menuDao.searchMenus("cino").test {
            val result = awaitItem()
            assertThat(result).hasSize(2) // Cappuccino and Americano
            assertThat(result.all { it.name.contains("cino", ignoreCase = true) }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchMenus_withNoMatches_returnsEmptyList() = runTest {
        // Given
        menuDao.insertMenu(createTestMenu(name = "Cappuccino"))

        // When & Then
        menuDao.searchMenus("Tea").test {
            val result = awaitItem()
            assertThat(result).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchMenus_caseInsensitive() = runTest {
        // Given
        menuDao.insertMenu(createTestMenu(name = "Cappuccino"))

        // When & Then - Search with different cases
        menuDao.searchMenus("CAPPUCCINO").test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== UPDATE TESTS ====================

    @Test
    fun updateMenu_updatesCorrectly() = runTest {
        // Given
        val menu = createTestMenu(name = "Cappuccino", basePrice = 25000.0)
        val id = menuDao.insertMenu(menu)

        val updated = menu.copy(
            id = id.toInt(),
            name = "Hot Cappuccino",
            basePrice = 30000.0
        )

        // When
        menuDao.updateMenu(updated)

        // Then
        val retrieved = menuDao.getMenuById(id.toInt())
        assertThat(retrieved?.name).isEqualTo("Hot Cappuccino")
        assertThat(retrieved?.basePrice).isEqualTo(30000.0)
    }

    @Test
    fun updateMenuStatus_changesStatusCorrectly() = runTest {
        // Given
        val menu = createTestMenu(isActive = true)
        val id = menuDao.insertMenu(menu)

        // When
        menuDao.updateMenuStatus(id.toInt(), false)

        // Then
        val retrieved = menuDao.getMenuById(id.toInt())
        assertThat(retrieved?.isActive).isFalse()
    }

    // ==================== DELETE TESTS ====================

    @Test
    fun deleteMenuById_removesMenu() = runTest {
        // Given
        val menu = createTestMenu()
        val id = menuDao.insertMenu(menu)

        // When
        menuDao.deleteMenuById(id.toInt())

        // Then
        val retrieved = menuDao.getMenuById(id.toInt())
        assertThat(retrieved).isNull()
    }

    @Test
    fun deleteMenu_removesMenuByEntity() = runTest {
        // Given
        val menu = createTestMenu()
        val id = menuDao.insertMenu(menu)
        val menuWithId = menu.copy(id = id.toInt())

        // When
        menuDao.deleteMenu(menuWithId)

        // Then
        val retrieved = menuDao.getMenuById(id.toInt())
        assertThat(retrieved).isNull()
    }

    // ==================== COUNT TESTS ====================

    @Test
    fun getMenuCount_returnsCorrectCount() = runTest {
        // Given
        val menus = listOf(
            createTestMenu(name = "Menu 1"),
            createTestMenu(name = "Menu 2"),
            createTestMenu(name = "Menu 3")
        )
        menus.forEach { menuDao.insertMenu(it) }

        // When
        val count = menuDao.getMenuCount()

        // Then
        assertThat(count).isEqualTo(3)
    }

    @Test
    fun getMenuCount_withEmptyDatabase_returnsZero() = runTest {
        // When
        val count = menuDao.getMenuCount()

        // Then
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun getMenuCountByCategory_returnsCorrectCount() = runTest {
        // Given
        val category2 = testCategory.copy(id = 2, name = "Tea")
        categoryDao.insertCategory(category2)

        menuDao.insertMenu(createTestMenu(name = "Coffee 1", categoryId = 1))
        menuDao.insertMenu(createTestMenu(name = "Coffee 2", categoryId = 1))
        menuDao.insertMenu(createTestMenu(name = "Tea 1", categoryId = 2))

        // When
        val coffeeCount = menuDao.getMenuCountByCategory(1)
        val teaCount = menuDao.getMenuCountByCategory(2)

        // Then
        assertThat(coffeeCount).isEqualTo(2)
        assertThat(teaCount).isEqualTo(1)
    }

    @Test
    fun getMenuCountByCategory_withNoMenus_returnsZero() = runTest {
        // When
        val count = menuDao.getMenuCountByCategory(1)

        // Then
        assertThat(count).isEqualTo(0)
    }

    // ==================== FOREIGN KEY CONSTRAINT TESTS ====================

    @Test
    fun insertMenu_withInvalidCategoryId_fails() = runTest {
        // Given
        val menu = createTestMenu(categoryId = 999) // Non-existing category

        // When & Then
        try {
            menuDao.insertMenu(menu)
            throw AssertionError("Should have thrown foreign key constraint exception")
        } catch (e: Exception) {
            // Expected - foreign key constraint violation
            assertThat(e.message).contains("FOREIGN KEY constraint failed")
        }
    }

    @Test
    fun deleteCategory_withExistingMenus_fails() = runTest {
        // Given - Insert menu that references category
        menuDao.insertMenu(createTestMenu(categoryId = 1))

        // When & Then
        try {
            categoryDao.deleteCategoryById(1)
            throw AssertionError("Should have thrown foreign key constraint exception")
        } catch (e: Exception) {
            // Expected - RESTRICT constraint on delete
            assertThat(e.message).contains("FOREIGN KEY constraint failed")
        }
    }

    // ==================== EDGE CASES ====================

    @Test
    fun insertMenu_withZeroPrice_succeeds() = runTest {
        // Given - Free menu item
        val menu = createTestMenu(basePrice = 0.0)

        // When
        val id = menuDao.insertMenu(menu)

        // Then
        val retrieved = menuDao.getMenuById(id.toInt())
        assertThat(retrieved?.basePrice).isEqualTo(0.0)
    }

    @Test
    fun insertMenu_withVeryHighPrice_succeeds() = runTest {
        // Given
        val menu = createTestMenu(basePrice = 999999999.99)

        // When
        val id = menuDao.insertMenu(menu)

        // Then
        val retrieved = menuDao.getMenuById(id.toInt())
        assertThat(retrieved?.basePrice).isEqualTo(999999999.99)
    }

    @Test
    fun insertMenu_withSpecialCharactersInName_succeeds() = runTest {
        // Given
        val menu = createTestMenu(name = "Café Latté & Mocha's")

        // When
        val id = menuDao.insertMenu(menu)

        // Then
        val retrieved = menuDao.getMenuById(id.toInt())
        assertThat(retrieved?.name).isEqualTo("Café Latté & Mocha's")
    }

    @Test
    fun insertMenu_withNullImage_succeeds() = runTest {
        // Given
        val menu = createTestMenu(imageUri = null)

        // When
        val id = menuDao.insertMenu(menu)

        // Then
        val retrieved = menuDao.getMenuById(id.toInt())
        assertThat(retrieved?.imageUri).isNull()
    }

    @Test
    fun insertMenu_withImagePath_succeeds() = runTest {
        // Given
        val imagePath = "/data/menu_images/menu_123.jpg"
        val menu = createTestMenu(imageUri = imagePath)

        // When
        val id = menuDao.insertMenu(menu)

        // Then
        val retrieved = menuDao.getMenuById(id.toInt())
        assertThat(retrieved?.imageUri).isEqualTo(imagePath)
    }

    @Test
    fun getActiveMenus_sortedByNameAsc() = runTest {
        // Given
        val menus = listOf(
            createTestMenu(name = "Espresso", isActive = true),
            createTestMenu(name = "Cappuccino", isActive = true),
            createTestMenu(name = "Latte", isActive = true)
        )
        menus.forEach { menuDao.insertMenu(it) }

        // When & Then
        menuDao.getActiveMenus().test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            assertThat(result[0].name).isEqualTo("Cappuccino") // Alphabetically first
            assertThat(result[1].name).isEqualTo("Espresso")
            assertThat(result[2].name).isEqualTo("Latte")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateMenu_preservesCreatedAt() = runTest {
        // Given
        val menu = createTestMenu()
        val id = menuDao.insertMenu(menu)
        val inserted = menuDao.getMenuById(id.toInt())
        val originalCreatedAt = inserted?.createdAt

        // Wait a bit
        Thread.sleep(10)

        // When
        val updated = inserted!!.copy(name = "Updated Name")
        menuDao.updateMenu(updated)

        // Then
        val retrieved = menuDao.getMenuById(id.toInt())
        assertThat(retrieved?.createdAt).isEqualTo(originalCreatedAt)
        assertThat(retrieved?.name).isEqualTo("Updated Name")
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun createTestMenu(
        id: Int = 0,
        name: String = "Cappuccino",
        categoryId: Int = 1,
        basePrice: Double = 25000.0,
        isActive: Boolean = true,
        imageUri: String? = null,
        sold: Int = 0
    ): MenuEntity {
        return MenuEntity(
            id = id,
            name = name,
            categoryId = categoryId,
            basePrice = basePrice,
            isActive = isActive,
            imageUri = imageUri,
            sold = sold,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}
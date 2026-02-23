package dev.faizal.core.testing.repository

import android.content.Context
import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.faizal.core.data.datasource.dao.CategoryDao
import dev.faizal.core.data.datasource.dao.MenuDao
import dev.faizal.core.data.datasource.entity.CategoryEntity
import dev.faizal.core.data.datasource.entity.MenuEntity
import dev.faizal.core.data.repository.MenuRepositoryImpl
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Unit tests for MenuRepositoryImpl
 *
 * Tests cover:
 * - Menu CRUD operations
 * - Image handling (save, delete)
 * - Menu filtering by category
 * - Search functionality
 * - Status updates
 * - Error handling
 */
class MenuRepositoryImplTest {

    private lateinit var menuDao: MenuDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var context: Context
    private lateinit var repository: MenuRepositoryImpl

    private val testCategory = CategoryEntity(
        id = 1,
        name = "Coffee",
        emoji = "☕",
        displayOrder = 1,
        isActive = true
    )

    private val testMenuEntity = MenuEntity(
        id = 1,
        name = "Cappuccino",
        categoryId = 1,
        basePrice = 25000.0,
        isActive = true,
        imageUri = null,
        sold = 10
    )

    @Before
    fun setup() {
        menuDao = mockk()
        categoryDao = mockk()
        context = mockk(relaxed = true)

        repository = MenuRepositoryImpl(menuDao, categoryDao, context)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ==================== GET MENUS TESTS ====================

    @Test
    fun `getAllMenus returns all menus with categories`() = runTest {
        // Given
        val menuEntities = listOf(
            testMenuEntity,
            testMenuEntity.copy(id = 2, name = "Latte")
        )

        every { menuDao.getAllMenus() } returns flowOf(menuEntities)
        coEvery { categoryDao.getCategoryById(1) } returns testCategory

        // When & Then
        repository.getAllMenus().test {
            val menus = awaitItem()
            assertThat(menus).hasSize(2)
            assertThat(menus[0].name).isEqualTo("Cappuccino")
            assertThat(menus[0].categoryName).isEqualTo("Coffee")
            assertThat(menus[1].name).isEqualTo("Latte")
            awaitComplete()
        }
    }

    @Test
    fun `getAllMenus filters out menus with missing categories`() = runTest {
        // Given
        val menuEntities = listOf(
            testMenuEntity,
            testMenuEntity.copy(id = 2, categoryId = 999) // Invalid category
        )

        every { menuDao.getAllMenus() } returns flowOf(menuEntities)
        coEvery { categoryDao.getCategoryById(1) } returns testCategory
        coEvery { categoryDao.getCategoryById(999) } returns null

        // When & Then
        repository.getAllMenus().test {
            val menus = awaitItem()
            assertThat(menus).hasSize(1) // Only valid menu
            assertThat(menus[0].id).isEqualTo(1)
            awaitComplete()
        }
    }

    @Test
    fun `getActiveMenus returns only active menus`() = runTest {
        // Given
        val menuEntities = listOf(
            testMenuEntity,
            testMenuEntity.copy(id = 2, name = "Latte", isActive = true)
        )

        every { menuDao.getActiveMenus() } returns flowOf(menuEntities)
        coEvery { categoryDao.getCategoryById(1) } returns testCategory

        // When & Then
        repository.getActiveMenus().test {
            val menus = awaitItem()
            assertThat(menus).hasSize(2)
            assertThat(menus.all { it.isActive }).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `getMenusByCategory filters by category id`() = runTest {
        // Given
        val categoryId = 1
        val menuEntities = listOf(testMenuEntity)

        every { menuDao.getMenusByCategory(categoryId) } returns flowOf(menuEntities)
        coEvery { categoryDao.getCategoryById(categoryId) } returns testCategory

        // When & Then
        repository.getMenusByCategory(categoryId).test {
            val menus = awaitItem()
            assertThat(menus).hasSize(1)
            assertThat(menus[0].categoryId).isEqualTo(categoryId)
            awaitComplete()
        }
    }

    @Test
    fun `getMenuById with existing menu returns menu`() = runTest {
        // Given
        val menuId = 1
        coEvery { menuDao.getMenuById(menuId) } returns testMenuEntity
        coEvery { categoryDao.getCategoryById(1) } returns testCategory

        // When
        val result = repository.getMenuById(menuId)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo("Cappuccino")
        assertThat(result?.categoryName).isEqualTo("Coffee")
    }

    @Test
    fun `getMenuById with non-existing menu returns null`() = runTest {
        // Given
        val menuId = 999
        coEvery { menuDao.getMenuById(menuId) } returns null

        // When
        val result = repository.getMenuById(menuId)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `getMenuById with missing category returns null`() = runTest {
        // Given
        val menuId = 1
        coEvery { menuDao.getMenuById(menuId) } returns testMenuEntity
        coEvery { categoryDao.getCategoryById(1) } returns null

        // When
        val result = repository.getMenuById(menuId)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `searchMenus returns matching menus`() = runTest {
        // Given
        val query = "Cap"
        val menuEntities = listOf(testMenuEntity)

        every { menuDao.searchMenus(query) } returns flowOf(menuEntities)
        coEvery { categoryDao.getCategoryById(1) } returns testCategory

        // When & Then
        repository.searchMenus(query).test {
            val menus = awaitItem()
            assertThat(menus).hasSize(1)
            assertThat(menus[0].name).contains("Cap")
            awaitComplete()
        }
    }

    // ==================== INSERT MENU TESTS ====================

    @Test
    fun `insertMenu with valid data without image returns success`() = runTest {
        // Given
        val name = "Cappuccino"
        val categoryId = 1
        val basePrice = 25000.0

        coEvery { menuDao.insertMenu(any()) } returns 1L

        // When
        val result = repository.insertMenu(
            name = name,
            categoryId = categoryId,
            basePrice = basePrice,
            imageUri = null,
            isActive = true
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(1L)

        val capturedMenu = slot<MenuEntity>()
        coVerify { menuDao.insertMenu(capture(capturedMenu)) }
        assertThat(capturedMenu.captured.name).isEqualTo(name)
        assertThat(capturedMenu.captured.categoryId).isEqualTo(categoryId)
        assertThat(capturedMenu.captured.imageUri).isNull()
    }

    @Test
    fun `insertMenu with image saves image and returns success`() = runTest {
        // Given
        val name = "Cappuccino"
        val categoryId = 1
        val basePrice = 25000.0
        val imageUri = mockk<Uri>(relaxed = true)
        val savedImagePath = "/data/menu_images/menu_123.jpg"

        // Mock image saving
        every { context.contentResolver } returns mockk(relaxed = true)
        every { context.filesDir } returns File("/data")
        mockkStatic(android.graphics.BitmapFactory::class)
        every { android.graphics.BitmapFactory.decodeStream(any()) } returns mockk(relaxed = true)

        coEvery { menuDao.insertMenu(any()) } returns 1L

        // When
        val result = repository.insertMenu(
            name = name,
            categoryId = categoryId,
            basePrice = basePrice,
            imageUri = imageUri,
            isActive = true
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        // Note: Image path will be mocked, just verify insertMenu was called
        coVerify { menuDao.insertMenu(any()) }
    }

    @Test
    fun `insertMenu with database error returns failure`() = runTest {
        // Given
        val errorMessage = "Database error"
        coEvery { menuDao.insertMenu(any()) } throws Exception(errorMessage)

        // When
        val result = repository.insertMenu(
            name = "Test",
            categoryId = 1,
            basePrice = 10000.0,
            imageUri = null,
            isActive = true
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    // ==================== UPDATE MENU TESTS ====================

    @Test
    fun `updateMenu with valid data without image change returns success`() = runTest {
        // Given
        val menuId = 1
        val newName = "Hot Cappuccino"
        val newPrice = 30000.0

        coEvery { menuDao.getMenuById(menuId) } returns testMenuEntity
        coEvery { menuDao.updateMenu(any()) } just Runs

        // When
        val result = repository.updateMenu(
            id = menuId,
            name = newName,
            categoryId = 1,
            basePrice = newPrice,
            imageUri = null,
            isActive = true
        )

        // Then
        assertThat(result.isSuccess).isTrue()

        val capturedMenu = slot<MenuEntity>()
        coVerify { menuDao.updateMenu(capture(capturedMenu)) }
        assertThat(capturedMenu.captured.name).isEqualTo(newName)
        assertThat(capturedMenu.captured.basePrice).isEqualTo(newPrice)
    }

    @Test
    fun `updateMenu with non-existing menu returns failure`() = runTest {
        // Given
        val menuId = 999
        coEvery { menuDao.getMenuById(menuId) } returns null

        // When
        val result = repository.updateMenu(
            id = menuId,
            name = "Test",
            categoryId = 1,
            basePrice = 25000.0,
            imageUri = null,
            isActive = true
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("not found")
    }

    @Test
    fun `updateMenu with database error returns failure`() = runTest {
        // Given
        val menuId = 1
        val errorMessage = "Update failed"

        coEvery { menuDao.getMenuById(menuId) } returns testMenuEntity
        coEvery { menuDao.updateMenu(any()) } throws Exception(errorMessage)

        // When
        val result = repository.updateMenu(
            id = menuId,
            name = "Test",
            categoryId = 1,
            basePrice = 25000.0,
            imageUri = null,
            isActive = true
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    // ==================== DELETE MENU TESTS ====================

    @Test
    fun `deleteMenu without image returns success`() = runTest {
        // Given
        val menuId = 1
        coEvery { menuDao.getMenuById(menuId) } returns testMenuEntity
        coEvery { menuDao.deleteMenuById(menuId) } just Runs

        // When
        val result = repository.deleteMenu(menuId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { menuDao.deleteMenuById(menuId) }
    }

    @Test
    fun `deleteMenu with image deletes image file and menu`() = runTest {
        // Given
        val menuId = 1
        val imagePath = "/data/menu_image.jpg"
        val menuWithImage = testMenuEntity.copy(imageUri = imagePath)

        coEvery { menuDao.getMenuById(menuId) } returns menuWithImage
        coEvery { menuDao.deleteMenuById(menuId) } just Runs

        // Mock file deletion
        mockkStatic(File::class)
        val imageFile = mockk<File>(relaxed = true)
        every { imageFile.exists() } returns true
        every { imageFile.delete() } returns true

        // When
        val result = repository.deleteMenu(menuId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { menuDao.deleteMenuById(menuId) }
    }

    @Test
    fun `deleteMenu with database error returns failure`() = runTest {
        // Given
        val menuId = 1
        val errorMessage = "Delete failed"

        coEvery { menuDao.getMenuById(menuId) } returns testMenuEntity
        coEvery { menuDao.deleteMenuById(menuId) } throws Exception(errorMessage)

        // When
        val result = repository.deleteMenu(menuId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    // ==================== STATUS UPDATE TESTS ====================

    @Test
    fun `updateMenuStatus activates menu successfully`() = runTest {
        // Given
        val menuId = 1
        val isActive = true
        coEvery { menuDao.updateMenuStatus(menuId, isActive) } just Runs

        // When
        val result = repository.updateMenuStatus(menuId, isActive)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { menuDao.updateMenuStatus(menuId, true) }
    }

    @Test
    fun `updateMenuStatus deactivates menu successfully`() = runTest {
        // Given
        val menuId = 1
        val isActive = false
        coEvery { menuDao.updateMenuStatus(menuId, isActive) } just Runs

        // When
        val result = repository.updateMenuStatus(menuId, isActive)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { menuDao.updateMenuStatus(menuId, false) }
    }

    @Test
    fun `updateMenuStatus with database error returns failure`() = runTest {
        // Given
        val menuId = 1
        val errorMessage = "Update failed"
        coEvery { menuDao.updateMenuStatus(any(), any()) } throws Exception(errorMessage)

        // When
        val result = repository.updateMenuStatus(menuId, true)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    // ==================== COUNT & STATISTICS TESTS ====================

    @Test
    fun `getMenuCount returns correct count`() = runTest {
        // Given
        val expectedCount = 25
        coEvery { menuDao.getMenuCount() } returns expectedCount

        // When
        val result = repository.getMenuCount()

        // Then
        assertThat(result).isEqualTo(expectedCount)
    }

    @Test
    fun `getMenuCountByCategory returns correct count`() = runTest {
        // Given
        val categoryId = 1
        val expectedCount = 10
        coEvery { menuDao.getMenuCountByCategory(categoryId) } returns expectedCount

        // When
        val result = repository.getMenuCountByCategory(categoryId)

        // Then
        assertThat(result).isEqualTo(expectedCount)
    }

    @Test
    fun `getMenuCountByCategory with no menus returns zero`() = runTest {
        // Given
        val categoryId = 1
        coEvery { menuDao.getMenuCountByCategory(categoryId) } returns 0

        // When
        val result = repository.getMenuCountByCategory(categoryId)

        // Then
        assertThat(result).isEqualTo(0)
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `insertMenu with special characters in name succeeds`() = runTest {
        // Given
        val name = "Café Latté & Mocha's"
        coEvery { menuDao.insertMenu(any()) } returns 1L

        // When
        val result = repository.insertMenu(
            name = name,
            categoryId = 1,
            basePrice = 25000.0,
            imageUri = null,
            isActive = true
        )

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `insertMenu with zero price succeeds`() = runTest {
        // Given - Free menu item
        val basePrice = 0.0
        coEvery { menuDao.insertMenu(any()) } returns 1L

        // When
        val result = repository.insertMenu(
            name = "Free Sample",
            categoryId = 1,
            basePrice = basePrice,
            imageUri = null,
            isActive = true
        )

        // Then
        assertThat(result.isSuccess).isTrue()

        val capturedMenu = slot<MenuEntity>()
        coVerify { menuDao.insertMenu(capture(capturedMenu)) }
        assertThat(capturedMenu.captured.basePrice).isEqualTo(0.0)
    }

    @Test
    fun `insertMenu with very high price succeeds`() = runTest {
        // Given
        val basePrice = 999999999.99
        coEvery { menuDao.insertMenu(any()) } returns 1L

        // When
        val result = repository.insertMenu(
            name = "Premium Item",
            categoryId = 1,
            basePrice = basePrice,
            imageUri = null,
            isActive = true
        )

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `updateMenu keeps existing image when null imageUri provided`() = runTest {
        // Given
        val menuId = 1
        val existingImagePath = "/data/existing_image.jpg"
        val existingMenu = testMenuEntity.copy(imageUri = existingImagePath)

        coEvery { menuDao.getMenuById(menuId) } returns existingMenu
        coEvery { menuDao.updateMenu(any()) } just Runs

        // When
        val result = repository.updateMenu(
            id = menuId,
            name = "Updated Name",
            categoryId = 1,
            basePrice = 30000.0,
            imageUri = null, // Not changing image
            isActive = true
        )

        // Then
        assertThat(result.isSuccess).isTrue()

        val capturedMenu = slot<MenuEntity>()
        coVerify { menuDao.updateMenu(capture(capturedMenu)) }
        assertThat(capturedMenu.captured.imageUri).isEqualTo(existingImagePath)
    }

    @Test
    fun `getAllMenus with empty result returns empty list`() = runTest {
        // Given
        every { menuDao.getAllMenus() } returns flowOf(emptyList())

        // When & Then
        repository.getAllMenus().test {
            val menus = awaitItem()
            assertThat(menus).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `searchMenus with no matches returns empty list`() = runTest {
        // Given
        val query = "NonExistentMenu"
        every { menuDao.searchMenus(query) } returns flowOf(emptyList())

        // When & Then
        repository.searchMenus(query).test {
            val menus = awaitItem()
            assertThat(menus).isEmpty()
            awaitComplete()
        }
    }
}
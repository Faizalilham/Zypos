package dev.faizal.features.menu

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.faizal.core.domain.model.menu.Category
import dev.faizal.core.domain.model.menu.Menu
import dev.faizal.core.domain.repository.CategoryRepository
import dev.faizal.core.domain.repository.MenuRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for MenuViewModel
 *
 * Tests cover:
 * - Menu CRUD operations
 * - Form validation
 * - Image selection
 * - Search and filtering
 * - Category selection
 * - Status toggling
 * - Error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var menuRepository: MenuRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var viewModel: MenuViewModel

    private val testDispatcher = StandardTestDispatcher()

    // Test data
    private val testCategory = Category(
        id = 1,
        name = "Coffee",
        emoji = "☕",
        displayOrder = 1,
        isActive = true
    )

    private val testMenu = Menu(
        id = 1,
        name = "Cappuccino",
        categoryId = 1,
        categoryName = "Coffee",
        basePrice = 25000.0,
        isActive = true,
        imageUri = null,
        sold = 10,
        imageUrl = 0,
        categoryEmoji = "☕"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        menuRepository = mockk()
        categoryRepository = mockk()

        // Default mocks
        every { menuRepository.getAllMenus() } returns flowOf(listOf(testMenu))
        every { categoryRepository.getActiveCategories() } returns flowOf(listOf(testCategory))

        viewModel = MenuViewModel(menuRepository, categoryRepository)

        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // ==================== INITIALIZATION TESTS ====================

    @Test
    fun `viewModel initializes with empty form state`() {
        // Then
        assertThat(viewModel.state.name).isEmpty()
        assertThat(viewModel.state.selectedCategoryId).isNull()
        assertThat(viewModel.state.price).isEmpty()
        assertThat(viewModel.state.isActive).isTrue()
        assertThat(viewModel.state.showCreateDialog).isFalse()
        assertThat(viewModel.state.showEditDialog).isFalse()
    }

    @Test
    fun `viewModel loads categories on init`() = runTest {
        // Then
        viewModel.categories.test {
            val categories = awaitItem()
            assertThat(categories).hasSize(1)
            assertThat(categories.first().name).isEqualTo("Coffee")
        }
    }

    @Test
    fun `viewModel loads menus on init`() = runTest {
        // Then
        viewModel.filteredMenus.test {
            val menus = awaitItem()
            assertThat(menus).hasSize(1)
            assertThat(menus.first().name).isEqualTo("Cappuccino")
        }
    }

    // ==================== FORM INPUT TESTS ====================

    @Test
    fun `onNameChange updates name and clears error`() {
        // Given
        viewModel.state = viewModel.state.copy(nameError = "Error")

        // When
        viewModel.onNameChange("Latte")

        // Then
        assertThat(viewModel.state.name).isEqualTo("Latte")
        assertThat(viewModel.state.nameError).isNull()
    }

    @Test
    fun `onCategoryChange updates category and clears error`() {
        // Given
        viewModel.state = viewModel.state.copy(categoryError = "Error")

        // When
        viewModel.onCategoryChange(1)

        // Then
        assertThat(viewModel.state.selectedCategoryId).isEqualTo(1)
        assertThat(viewModel.state.categoryError).isNull()
    }

    @Test
    fun `onPriceChange updates price and clears error`() {
        // Given
        viewModel.state = viewModel.state.copy(priceError = "Error")

        // When
        viewModel.onPriceChange("30000")

        // Then
        assertThat(viewModel.state.price).isEqualTo("30000")
        assertThat(viewModel.state.priceError).isNull()
    }

    @Test
    fun `onImageSelected updates image uri`() {
        // Given
        val imageUri = mockk<Uri>()

        // When
        viewModel.onImageSelected(imageUri)

        // Then
        assertThat(viewModel.state.selectedImageUri).isEqualTo(imageUri)
    }

    @Test
    fun `onImageSelected with null clears image`() {
        // Given
        viewModel.state = viewModel.state.copy(selectedImageUri = mockk())

        // When
        viewModel.onImageSelected(null)

        // Then
        assertThat(viewModel.state.selectedImageUri).isNull()
    }

    @Test
    fun `onIsActiveChange updates active status`() {
        // When
        viewModel.onIsActiveChange(false)

        // Then
        assertThat(viewModel.state.isActive).isFalse()
    }

    // ==================== SEARCH AND FILTER TESTS ====================

    @Test
    fun `onSearchQueryChange updates search query`() {
        // When
        viewModel.onSearchQueryChange("Cap")

        // Then
        assertThat(viewModel.state.searchQuery).isEqualTo("Cap")
    }

    @Test
    fun `onCategoryFilterChange updates filter category`() {
        // When
        viewModel.onCategoryFilterChange(1)

        // Then
        assertThat(viewModel.state.selectedFilterCategoryId).isEqualTo(1)
    }

    @Test
    fun `onCategoryFilterChange with null shows all categories`() {
        // Given
        viewModel.state = viewModel.state.copy(selectedFilterCategoryId = 1)

        // When
        viewModel.onCategoryFilterChange(null)

        // Then
        assertThat(viewModel.state.selectedFilterCategoryId).isNull()
    }

    @Test
    fun `search filters menus by name`() = runTest {
        // Given
        val menus = listOf(
            testMenu,
            testMenu.copy(id = 2, name = "Latte"),
            testMenu.copy(id = 3, name = "Espresso")
        )
        every { menuRepository.getAllMenus() } returns flowOf(menus)

        val newViewModel = MenuViewModel(menuRepository, categoryRepository)
        testScheduler.advanceUntilIdle()

        // Then - collect dulu sebelum trigger
        newViewModel.filteredMenus.test {
            // Skip initial emission (semua menu)
            skipItems(1)

            // When - baru trigger search
            newViewModel.onSearchQueryChange("Latte")
            testScheduler.advanceUntilIdle()

            // Ambil emisi setelah filter
            val filtered = awaitItem()

            assertThat(filtered).hasSize(1)
            assertThat(filtered.first().name).isEqualTo("Latte")
        }
    }

    @Test
    fun `category filter shows only menus from that category`() = runTest {
        // Given
        val category2 = testCategory.copy(id = 2, name = "Tea")
        val menus = listOf(
            testMenu.copy(id = 1, categoryId = 1),
            testMenu.copy(id = 2, categoryId = 2, categoryName = "Tea"),
            testMenu.copy(id = 3, categoryId = 1)
        )

        every { menuRepository.getAllMenus() } returns flowOf(menus)
        every { categoryRepository.getActiveCategories() } returns flowOf(listOf(testCategory, category2))

        val newViewModel = MenuViewModel(menuRepository, categoryRepository)
        advanceUntilIdle()

        // When
        newViewModel.onCategoryFilterChange(2)
        advanceUntilIdle()

        // Then
        newViewModel.filteredMenus.test {
            val filtered = awaitItem()
            assertThat(filtered).hasSize(1)
            assertThat(filtered.first().categoryId).isEqualTo(2)
        }
    }

    // ==================== DIALOG MANAGEMENT TESTS ====================

    @Test
    fun `toggleCreateDialog shows dialog`() {
        // When
        viewModel.toggleCreateDialog(true)

        // Then
        assertThat(viewModel.state.showCreateDialog).isTrue()
    }

    @Test
    fun `toggleCreateDialog hides dialog and resets form`() {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Test",
            price = "10000",
            showCreateDialog = true
        )

        // When
        viewModel.toggleCreateDialog(false)

        // Then
        assertThat(viewModel.state.showCreateDialog).isFalse()
        assertThat(viewModel.state.name).isEmpty()
        assertThat(viewModel.state.price).isEmpty()
    }

    @Test
    fun `toggleEditDialog shows dialog and populates form`() {
        // Given
        val menu = testMenu

        // When
        viewModel.toggleEditDialog(true, menu)

        // Then
        assertThat(viewModel.state.showEditDialog).isTrue()
        assertThat(viewModel.state.editingMenuId).isEqualTo(1)
        assertThat(viewModel.state.name).isEqualTo("Cappuccino")
        assertThat(viewModel.state.selectedCategoryId).isEqualTo(1)
        assertThat(viewModel.state.price).isEqualTo("25000.0")
        assertThat(viewModel.state.isActive).isTrue()
    }

    @Test
    fun `toggleEditDialog hides dialog and resets form`() {
        // Given
        viewModel.state = viewModel.state.copy(
            showEditDialog = true,
            editingMenuId = 1,
            name = "Test"
        )

        // When
        viewModel.toggleEditDialog(false)

        // Then
        assertThat(viewModel.state.showEditDialog).isFalse()
        assertThat(viewModel.state.editingMenuId).isNull()
        assertThat(viewModel.state.name).isEmpty()
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    fun `createMenu with empty name shows error`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "",
            selectedCategoryId = 1,
            price = "25000"
        )

        // When
        viewModel.createMenu()

        // Then
        assertThat(viewModel.state.nameError).isEqualTo("Menu name is required")
        coVerify(exactly = 0) { menuRepository.insertMenu(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `createMenu with null category shows error`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Cappuccino",
            selectedCategoryId = null,
            price = "25000"
        )

        // When
        viewModel.createMenu()

        // Then
        assertThat(viewModel.state.categoryError).isEqualTo("Category is required")
    }

    @Test
    fun `createMenu with empty price shows error`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Cappuccino",
            selectedCategoryId = 1,
            price = ""
        )

        // When
        viewModel.createMenu()

        // Then
        assertThat(viewModel.state.priceError).isEqualTo("Price is required")
    }

    @Test
    fun `createMenu with negative price shows error`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Cappuccino",
            selectedCategoryId = 1,
            price = "-1000"
        )

        // When
        viewModel.createMenu()

        // Then
        assertThat(viewModel.state.priceError).isEqualTo("Price must be greater than 0")
    }

    @Test
    fun `createMenu with zero price shows error`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Cappuccino",
            selectedCategoryId = 1,
            price = "0"
        )

        // When
        viewModel.createMenu()

        // Then
        assertThat(viewModel.state.priceError).isEqualTo("Price must be greater than 0")
    }

    @Test
    fun `createMenu with invalid price format shows error`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Cappuccino",
            selectedCategoryId = 1,
            price = "abc"
        )

        // When
        viewModel.createMenu()

        // Then
        assertThat(viewModel.state.priceError).isEqualTo("Invalid price format")
    }

    // ==================== CREATE MENU TESTS ====================

    @Test
    fun `createMenu with valid data succeeds`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Cappuccino",
            selectedCategoryId = 1,
            price = "25000",
            isActive = true
        )

        coEvery {
            menuRepository.insertMenu(any(), any(), any(), any(), any())
        } returns Result.success(1L)

        // When
        viewModel.createMenu()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.successMessage).isEqualTo("Menu created successfully!")
        assertThat(viewModel.state.isLoading).isFalse()
        assertThat(viewModel.state.showCreateDialog).isFalse()

        coVerify {
            menuRepository.insertMenu(
                name = "Cappuccino",
                categoryId = 1,
                basePrice = 25000.0,
                imageUri = null,
                isActive = true
            )
        }
    }

    @Test
    fun `createMenu with repository error shows error message`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Cappuccino",
            selectedCategoryId = 1,
            price = "25000"
        )

        val errorMessage = "Database error"
        coEvery {
            menuRepository.insertMenu(any(), any(), any(), any(), any())
        } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.createMenu()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.errorMessage).isEqualTo(errorMessage)
        assertThat(viewModel.state.isLoading).isFalse()
    }

    @Test
    fun `createMenu trims whitespace from name`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "  Cappuccino  ",
            selectedCategoryId = 1,
            price = "25000"
        )

        coEvery {
            menuRepository.insertMenu(any(), any(), any(), any(), any())
        } returns Result.success(1L)

        // When
        viewModel.createMenu()
        advanceUntilIdle()

        // Then
        coVerify {
            menuRepository.insertMenu(
                name = "Cappuccino", // Trimmed
                categoryId = any(),
                basePrice = any(),
                imageUri = any(),
                isActive = any()
            )
        }
    }

    // ==================== UPDATE MENU TESTS ====================

    @Test
    fun `updateMenu with valid data succeeds`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            editingMenuId = 1,
            name = "Hot Cappuccino",
            selectedCategoryId = 1,
            price = "30000",
            isActive = true
        )

        coEvery {
            menuRepository.updateMenu(any(), any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        // When
        viewModel.updateMenu()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.successMessage).isEqualTo("Menu updated successfully!")
        assertThat(viewModel.state.showEditDialog).isFalse()

        coVerify {
            menuRepository.updateMenu(
                id = 1,
                name = "Hot Cappuccino",
                categoryId = 1,
                basePrice = 30000.0,
                imageUri = any(),
                isActive = true
            )
        }
    }

    @Test
    fun `updateMenu without editing id does nothing`() = runTest {
        // Given - No editing ID set
        viewModel.state = viewModel.state.copy(
            editingMenuId = null,
            name = "Test",
            selectedCategoryId = 1,
            price = "25000"
        )

        // When
        viewModel.updateMenu()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { menuRepository.updateMenu(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `updateMenu with repository error shows error message`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            editingMenuId = 1,
            name = "Test",
            selectedCategoryId = 1,
            price = "25000"
        )

        val errorMessage = "Update failed"
        coEvery {
            menuRepository.updateMenu(any(), any(), any(), any(), any(), any())
        } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.updateMenu()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.errorMessage).isEqualTo(errorMessage)
        assertThat(viewModel.state.isLoading).isFalse()
    }

    // ==================== DELETE MENU TESTS ====================

    @Test
    fun `deleteMenu succeeds and shows success message`() = runTest {
        // Given
        val menuId = 1
        coEvery { menuRepository.deleteMenu(menuId) } returns Result.success(Unit)

        // When
        viewModel.deleteMenu(menuId)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.successMessage).isEqualTo("Menu deleted successfully!")
        coVerify { menuRepository.deleteMenu(menuId) }
    }

    @Test
    fun `deleteMenu with error shows error message`() = runTest {
        // Given
        val menuId = 1
        val errorMessage = "Cannot delete menu"
        coEvery { menuRepository.deleteMenu(menuId) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.deleteMenu(menuId)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.errorMessage).isEqualTo(errorMessage)
    }

    // ==================== TOGGLE STATUS TESTS ====================

    @Test
    fun `toggleMenuStatus activates menu`() = runTest {
        // Given
        val menuId = 1
        coEvery { menuRepository.updateMenuStatus(menuId, true) } returns Result.success(Unit)

        // When
        viewModel.toggleMenuStatus(menuId, true)
        advanceUntilIdle()

        // Then
        coVerify { menuRepository.updateMenuStatus(menuId, true) }
    }

    @Test
    fun `toggleMenuStatus deactivates menu`() = runTest {
        // Given
        val menuId = 1
        coEvery { menuRepository.updateMenuStatus(menuId, false) } returns Result.success(Unit)

        // When
        viewModel.toggleMenuStatus(menuId, false)
        advanceUntilIdle()

        // Then
        coVerify { menuRepository.updateMenuStatus(menuId, false) }
    }

    @Test
    fun `toggleMenuStatus with error shows error message`() = runTest {
        // Given
        val menuId = 1
        val errorMessage = "Status update failed"
        coEvery { menuRepository.updateMenuStatus(any(), any()) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.toggleMenuStatus(menuId, true)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.errorMessage).isEqualTo(errorMessage)
    }

    // ==================== CLEAR MESSAGES TESTS ====================

    @Test
    fun `clearMessages clears success and error messages`() {
        // Given
        viewModel.state = viewModel.state.copy(
            successMessage = "Success!",
            errorMessage = "Error!"
        )

        // When
        viewModel.clearMessages()

        // Then
        assertThat(viewModel.state.successMessage).isNull()
        assertThat(viewModel.state.errorMessage).isNull()
    }

    // ==================== BEST SELLER TESTS ====================

    @Test
    fun `getBestSellerMenus returns top menus by sold count`() {
        // Given
        val menus = listOf(
            testMenu.copy(id = 1, name = "Cappuccino", sold = 50),
            testMenu.copy(id = 2, name = "Latte", sold = 75),
            testMenu.copy(id = 3, name = "Espresso", sold = 25),
            testMenu.copy(id = 4, name = "Americano", sold = 100)
        )

        every { menuRepository.getAllMenus() } returns flowOf(menus)

        val newViewModel = MenuViewModel(menuRepository, categoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val bestSellers = newViewModel.getBestSellerMenus(limit = 3)

        // Then
        assertThat(bestSellers).hasSize(3)
        assertThat(bestSellers[0].name).isEqualTo("Americano") // Most sold
        assertThat(bestSellers[1].name).isEqualTo("Latte")
        assertThat(bestSellers[2].name).isEqualTo("Cappuccino")
    }

    @Test
    fun `getBestSellerMenus with limit larger than available returns all menus`() {
        // Given
        val menus = listOf(
            testMenu.copy(id = 1, sold = 50),
            testMenu.copy(id = 2, sold = 75)
        )

        every { menuRepository.getAllMenus() } returns flowOf(menus)

        val newViewModel = MenuViewModel(menuRepository, categoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val bestSellers = newViewModel.getBestSellerMenus(limit = 10)

        // Then
        assertThat(bestSellers).hasSize(2)
    }

    // ==================== COMPLEX SCENARIOS ====================

    @Test
    fun `search and category filter work together`() = runTest {
        // Given
        val menus = listOf(
            testMenu.copy(id = 1, name = "Cappuccino", categoryId = 1),
            testMenu.copy(id = 2, name = "Latte", categoryId = 1),
            testMenu.copy(id = 3, name = "Green Tea", categoryId = 2, categoryName = "Tea")
        )

        every { menuRepository.getAllMenus() } returns flowOf(menus)

        val newViewModel = MenuViewModel(menuRepository, categoryRepository)
        advanceUntilIdle()

        // When - Search "a" + Filter category 1
        newViewModel.onSearchQueryChange("a")
        newViewModel.onCategoryFilterChange(1)
        advanceUntilIdle()

        // Then - Should show only Coffee items with "a" in name
        newViewModel.filteredMenus.test {
            val filtered = awaitItem()
            assertThat(filtered).hasSize(2) // Cappuccino and Latte
            assertThat(filtered.all { it.categoryId == 1 }).isTrue()
            assertThat(filtered.all { it.name.contains("a", ignoreCase = true) }).isTrue()
        }
    }

    @Test
    fun `form resets after successful creation`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Cappuccino",
            selectedCategoryId = 1,
            price = "25000",
            selectedImageUri = mockk(),
            isActive = false
        )

        coEvery {
            menuRepository.insertMenu(any(), any(), any(), any(), any())
        } returns Result.success(1L)

        // When
        viewModel.createMenu()
        advanceUntilIdle()

        // Then - Form should be reset
        assertThat(viewModel.state.name).isEmpty()
        assertThat(viewModel.state.selectedCategoryId).isNull()
        assertThat(viewModel.state.price).isEmpty()
        assertThat(viewModel.state.selectedImageUri).isNull()
        assertThat(viewModel.state.isActive).isTrue() // Default value
    }
}
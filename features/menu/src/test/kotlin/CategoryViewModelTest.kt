package dev.faizal.features.menu

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.faizal.core.domain.model.menu.Category
import dev.faizal.core.domain.repository.CategoryRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for CategoryViewModel
 *
 * Tests cover:
 * - Category CRUD operations
 * - Form validation
 * - Display order management
 * - Status toggling
 * - Menu count tracking
 * - Error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CategoryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var viewModel: CategoryViewModel

    private val testDispatcher = StandardTestDispatcher()

    // Test data
    private val testCategory = Category(
        id = 1,
        name = "Coffee",
        emoji = "☕",
        displayOrder = 1,
        isActive = true
    )

    private val testCategoryWithCount = CategoryWithCount(
        category = testCategory,
        menuCount = 5
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        categoryRepository = mockk()

        // Default mock
        every { categoryRepository.getAllCategories() } returns flowOf(listOf(testCategory))
        coEvery { categoryRepository.getMenuCountByCategory(any()) } returns 5

        viewModel = CategoryViewModel(categoryRepository)

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
        assertThat(viewModel.state.emoji).isEmpty()
        assertThat(viewModel.state.displayOrder).isEqualTo("0")
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
            assertThat(categories.first().category.name).isEqualTo("Coffee")
            assertThat(categories.first().menuCount).isEqualTo(5)
        }
    }

    // ==================== FORM INPUT TESTS ====================

    @Test
    fun `onNameChange updates name and clears error`() {
        // Given
        viewModel.state = viewModel.state.copy(nameError = "Error")

        // When
        viewModel.onNameChange("Tea")

        // Then
        assertThat(viewModel.state.name).isEqualTo("Tea")
        assertThat(viewModel.state.nameError).isNull()
    }

    @Test
    fun `onEmojiChange updates emoji and clears error`() {
        // Given
        viewModel.state = viewModel.state.copy(emojiError = "Error")

        // When
        viewModel.onEmojiChange("🍵")

        // Then
        assertThat(viewModel.state.emoji).isEqualTo("🍵")
        assertThat(viewModel.state.emojiError).isNull()
    }

    @Test
    fun `onDisplayOrderChange updates display order and clears error`() {
        // Given
        viewModel.state = viewModel.state.copy(displayOrderError = "Error")

        // When
        viewModel.onDisplayOrderChange("5")

        // Then
        assertThat(viewModel.state.displayOrder).isEqualTo("5")
        assertThat(viewModel.state.displayOrderError).isNull()
    }

    @Test
    fun `onIsActiveChange updates active status`() {
        // When
        viewModel.onIsActiveChange(false)

        // Then
        assertThat(viewModel.state.isActive).isFalse()
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
            emoji = "🎯",
            displayOrder = "5",
            showCreateDialog = true
        )

        // When
        viewModel.toggleCreateDialog(false)

        // Then
        assertThat(viewModel.state.showCreateDialog).isFalse()
        assertThat(viewModel.state.name).isEmpty()
        assertThat(viewModel.state.emoji).isEmpty()
        assertThat(viewModel.state.displayOrder).isEqualTo("0")
    }

    @Test
    fun `toggleEditDialog shows dialog and populates form`() {
        // Given
        val categoryWithCount = testCategoryWithCount

        // When
        viewModel.toggleEditDialog(true, categoryWithCount)

        // Then
        assertThat(viewModel.state.showEditDialog).isTrue()
        assertThat(viewModel.state.editingCategoryId).isEqualTo(1)
        assertThat(viewModel.state.name).isEqualTo("Coffee")
        assertThat(viewModel.state.emoji).isEqualTo("☕")
        assertThat(viewModel.state.displayOrder).isEqualTo("1")
        assertThat(viewModel.state.isActive).isTrue()
    }

    @Test
    fun `toggleEditDialog hides dialog and resets form`() {
        // Given
        viewModel.state = viewModel.state.copy(
            showEditDialog = true,
            editingCategoryId = 1,
            name = "Test"
        )

        // When
        viewModel.toggleEditDialog(false)

        // Then
        assertThat(viewModel.state.showEditDialog).isFalse()
        assertThat(viewModel.state.editingCategoryId).isNull()
        assertThat(viewModel.state.name).isEmpty()
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    fun `createCategory with empty name shows error`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "",
            emoji = "☕",
            displayOrder = "1"
        )

        // When
        viewModel.createCategory()

        // Then
        assertThat(viewModel.state.nameError).isEqualTo("Category name is required")
        coVerify(exactly = 0) { categoryRepository.insertCategory(any(), any(), any()) }
    }

    @Test
    fun `createCategory with empty emoji shows error`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Coffee",
            emoji = "",
            displayOrder = "1"
        )

        // When
        viewModel.createCategory()

        // Then
        assertThat(viewModel.state.emojiError).isEqualTo("Emoji is required")
    }

    @Test
    fun `createCategory with negative display order shows error`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Coffee",
            emoji = "☕",
            displayOrder = "-1"
        )

        // When
        viewModel.createCategory()

        // Then
        assertThat(viewModel.state.displayOrderError).isEqualTo("Display order must be a positive number")
    }

    @Test
    fun `createCategory with invalid display order shows error`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Coffee",
            emoji = "☕",
            displayOrder = "abc"
        )

        // When
        viewModel.createCategory()

        // Then
        assertThat(viewModel.state.displayOrderError).isEqualTo("Display order must be a positive number")
    }

    // ==================== CREATE CATEGORY TESTS ====================

    @Test
    fun `createCategory with valid data succeeds`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Coffee",
            emoji = "☕",
            displayOrder = "1"
        )

        coEvery {
            categoryRepository.insertCategory(any(), any(), any())
        } returns Result.success(1L)

        // When
        viewModel.createCategory()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.successMessage).isEqualTo("Category created successfully!")
        assertThat(viewModel.state.isLoading).isFalse()
        assertThat(viewModel.state.showCreateDialog).isFalse()

        coVerify {
            categoryRepository.insertCategory(
                name = "Coffee",
                emoji = "☕",
                displayOrder = 1
            )
        }
    }

    @Test
    fun `createCategory with repository error shows error message`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Coffee",
            emoji = "☕",
            displayOrder = "1"
        )

        val errorMessage = "Category already exists"
        coEvery {
            categoryRepository.insertCategory(any(), any(), any())
        } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.createCategory()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.errorMessage).isEqualTo(errorMessage)
        assertThat(viewModel.state.isLoading).isFalse()
    }

    @Test
    fun `createCategory trims whitespace from name`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "  Coffee  ",
            emoji = "☕",
            displayOrder = "1"
        )

        coEvery {
            categoryRepository.insertCategory(any(), any(), any())
        } returns Result.success(1L)

        // When
        viewModel.createCategory()
        advanceUntilIdle()

        // Then
        coVerify {
            categoryRepository.insertCategory(
                name = "Coffee", // Trimmed
                emoji = any(),
                displayOrder = any()
            )
        }
    }

    @Test
    fun `createCategory trims whitespace from emoji`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Coffee",
            emoji = "  ☕  ",
            displayOrder = "1"
        )

        coEvery {
            categoryRepository.insertCategory(any(), any(), any())
        } returns Result.success(1L)

        // When
        viewModel.createCategory()
        advanceUntilIdle()

        // Then
        coVerify {
            categoryRepository.insertCategory(
                name = any(),
                emoji = "☕", // Trimmed
                displayOrder = any()
            )
        }
    }

    // ==================== UPDATE CATEGORY TESTS ====================

    @Test
    fun `updateCategory with valid data succeeds`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            editingCategoryId = 1,
            name = "Hot Coffee",
            emoji = "🔥☕",
            displayOrder = "2",
            isActive = true
        )

        coEvery {
            categoryRepository.updateCategory(any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        // When
        viewModel.updateCategory()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.successMessage).isEqualTo("Category updated successfully!")
        assertThat(viewModel.state.showEditDialog).isFalse()

        coVerify {
            categoryRepository.updateCategory(
                id = 1,
                name = "Hot Coffee",
                emoji = "🔥☕",
                displayOrder = 2,
                isActive = true
            )
        }
    }

    @Test
    fun `updateCategory without editing id does nothing`() = runTest {
        // Given - No editing ID set
        viewModel.state = viewModel.state.copy(
            editingCategoryId = null,
            name = "Test",
            emoji = "🎯",
            displayOrder = "1"
        )

        // When
        viewModel.updateCategory()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { categoryRepository.updateCategory(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `updateCategory with repository error shows error message`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            editingCategoryId = 1,
            name = "Test",
            emoji = "🎯",
            displayOrder = "1"
        )

        val errorMessage = "Category name already exists"
        coEvery {
            categoryRepository.updateCategory(any(), any(), any(), any(), any())
        } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.updateCategory()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.errorMessage).isEqualTo(errorMessage)
        assertThat(viewModel.state.isLoading).isFalse()
    }

    // ==================== DELETE CATEGORY TESTS ====================

    @Test
    fun `deleteCategory succeeds and shows success message`() = runTest {
        // Given
        val categoryId = 1
        coEvery { categoryRepository.deleteCategory(categoryId) } returns Result.success(Unit)

        // When
        viewModel.deleteCategory(categoryId)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.successMessage).isEqualTo("Category deleted successfully!")
        coVerify { categoryRepository.deleteCategory(categoryId) }
    }

    @Test
    fun `deleteCategory with existing menus shows error`() = runTest {
        // Given
        val categoryId = 1
        val errorMessage = "Cannot delete category. It is used by 5 menu item(s)"
        coEvery { categoryRepository.deleteCategory(categoryId) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.deleteCategory(categoryId)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.errorMessage).isEqualTo(errorMessage)
    }

    @Test
    fun `deleteCategory with repository error shows error message`() = runTest {
        // Given
        val categoryId = 1
        val errorMessage = "Database error"
        coEvery { categoryRepository.deleteCategory(categoryId) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.deleteCategory(categoryId)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.errorMessage).isEqualTo(errorMessage)
    }

    // ==================== TOGGLE STATUS TESTS ====================

    @Test
    fun `toggleCategoryStatus activates category`() = runTest {
        // Given
        val categoryId = 1
        coEvery { categoryRepository.updateCategoryStatus(categoryId, true) } returns Result.success(Unit)

        // When
        viewModel.toggleCategoryStatus(categoryId, true)
        advanceUntilIdle()

        // Then
        coVerify { categoryRepository.updateCategoryStatus(categoryId, true) }
    }

    @Test
    fun `toggleCategoryStatus deactivates category`() = runTest {
        // Given
        val categoryId = 1
        coEvery { categoryRepository.updateCategoryStatus(categoryId, false) } returns Result.success(Unit)

        // When
        viewModel.toggleCategoryStatus(categoryId, false)
        advanceUntilIdle()

        // Then
        coVerify { categoryRepository.updateCategoryStatus(categoryId, false) }
    }

    @Test
    fun `toggleCategoryStatus with error shows error message`() = runTest {
        // Given
        val categoryId = 1
        val errorMessage = "Status update failed"
        coEvery { categoryRepository.updateCategoryStatus(any(), any()) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.toggleCategoryStatus(categoryId, true)
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

    // ==================== LOAD CATEGORIES TESTS ====================

    @Test
    fun `loadCategories fetches categories with menu counts`() = runTest {
        // Given
        val categories = listOf(
            testCategory,
            testCategory.copy(id = 2, name = "Tea")
        )

        every { categoryRepository.getAllCategories() } returns flowOf(categories)
        coEvery { categoryRepository.getMenuCountByCategory(1) } returns 5
        coEvery { categoryRepository.getMenuCountByCategory(2) } returns 3

        // When
        viewModel.loadCategories()
        advanceUntilIdle()

        // Then
        viewModel.categories.test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result[0].menuCount).isEqualTo(5)
            assertThat(result[1].menuCount).isEqualTo(3)
        }
    }

    @Test
    fun `loadCategories with empty result returns empty list`() = runTest {
        // Given
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        // When
        viewModel.loadCategories()
        advanceUntilIdle()

        // Then
        viewModel.categories.test {
            val result = awaitItem()
            assertThat(result).isEmpty()
        }
    }

    // ==================== DISPLAY ORDER VALIDATION ====================

    @Test
    fun `createCategory with zero display order succeeds`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Coffee",
            emoji = "☕",
            displayOrder = "0"
        )

        coEvery {
            categoryRepository.insertCategory(any(), any(), any())
        } returns Result.success(1L)

        // When
        viewModel.createCategory()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.successMessage).isNotNull()
        coVerify {
            categoryRepository.insertCategory(
                name = any(),
                emoji = any(),
                displayOrder = 0
            )
        }
    }

    @Test
    fun `createCategory with large display order succeeds`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Coffee",
            emoji = "☕",
            displayOrder = "9999"
        )

        coEvery {
            categoryRepository.insertCategory(any(), any(), any())
        } returns Result.success(1L)

        // When
        viewModel.createCategory()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.successMessage).isNotNull()
        coVerify {
            categoryRepository.insertCategory(
                name = any(),
                emoji = any(),
                displayOrder = 9999
            )
        }
    }

    // ==================== COMPLEX SCENARIOS ====================

    @Test
    fun `form resets after successful creation`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Coffee",
            emoji = "☕",
            displayOrder = "5",
            isActive = false
        )

        coEvery {
            categoryRepository.insertCategory(any(), any(), any())
        } returns Result.success(1L)

        // When
        viewModel.createCategory()
        advanceUntilIdle()

        // Then - Form should be reset
        assertThat(viewModel.state.name).isEmpty()
        assertThat(viewModel.state.emoji).isEmpty()
        assertThat(viewModel.state.displayOrder).isEqualTo("0")
        assertThat(viewModel.state.isActive).isTrue() // Default value
    }

    @Test
    fun `can create multiple categories sequentially`() = runTest {
        // Given
        coEvery {
            categoryRepository.insertCategory(any(), any(), any())
        } returns Result.success(1L)

        // When - Create first category
        viewModel.state = viewModel.state.copy(
            name = "Coffee",
            emoji = "☕",
            displayOrder = "1"
        )
        viewModel.createCategory()
        advanceUntilIdle()

        // Clear success message
        viewModel.clearMessages()

        // When - Create second category
        viewModel.state = viewModel.state.copy(
            name = "Tea",
            emoji = "🍵",
            displayOrder = "2"
        )
        viewModel.createCategory()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.successMessage).isEqualTo("Category created successfully!")

        coVerify(exactly = 2) {
            categoryRepository.insertCategory(any(), any(), any())
        }
    }

    @Test
    fun `loading state is set during operations`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Coffee",
            emoji = "☕",
            displayOrder = "1"
        )

        coEvery {
            categoryRepository.insertCategory(any(), any(), any())
        } coAnswers {
            // Simulate slow operation
            kotlinx.coroutines.delay(100)
            Result.success(1L)
        }

        // When
        viewModel.createCategory()

        // Then - Check loading state before completion
        assertThat(viewModel.state.isLoading).isTrue()

        advanceUntilIdle()

        // Then - Loading should be false after completion
        assertThat(viewModel.state.isLoading).isFalse()
    }

    // ==================== SPECIAL CHARACTERS TESTS ====================

    @Test
    fun `createCategory with emoji in name succeeds`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Coffee ☕",
            emoji = "☕",
            displayOrder = "1"
        )

        coEvery {
            categoryRepository.insertCategory(any(), any(), any())
        } returns Result.success(1L)

        // When
        viewModel.createCategory()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.successMessage).isNotNull()
    }

    @Test
    fun `createCategory with special characters succeeds`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Coffee & Tea's",
            emoji = "☕",
            displayOrder = "1"
        )

        coEvery {
            categoryRepository.insertCategory(any(), any(), any())
        } returns Result.success(1L)

        // When
        viewModel.createCategory()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.successMessage).isNotNull()
    }

    @Test
    fun `createCategory with multiple emojis succeeds`() = runTest {
        // Given
        viewModel.state = viewModel.state.copy(
            name = "Coffee",
            emoji = "☕🔥",
            displayOrder = "1"
        )

        coEvery {
            categoryRepository.insertCategory(any(), any(), any())
        } returns Result.success(1L)

        // When
        viewModel.createCategory()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.successMessage).isNotNull()
    }
}
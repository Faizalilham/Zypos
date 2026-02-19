package dev.faizal.features.menu



import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizal.core.domain.model.menu.Category
import dev.faizal.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    var state by mutableStateOf(CategoryState())
        private set

    private val _categories = MutableStateFlow<List<CategoryWithCount>>(emptyList())
    val categories: StateFlow<List<CategoryWithCount>> = _categories.asStateFlow()

    init {
        loadCategories()
    }

    // ==================== PUBLIC METHODS ====================

    fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categoryList ->
                // Get menu count for each category
                val categoriesWithCount = categoryList.map { category ->
                    val menuCount = categoryRepository.getMenuCountByCategory(category.id)
                    CategoryWithCount(category, menuCount)
                }
                _categories.value = categoriesWithCount
            }
        }
    }

    fun onNameChange(name: String) {
        state = state.copy(name = name, nameError = null)
    }

    fun onEmojiChange(emoji: String) {
        state = state.copy(emoji = emoji, emojiError = null)
    }

    fun onDisplayOrderChange(order: String) {
        state = state.copy(displayOrder = order, displayOrderError = null)
    }

    fun onIsActiveChange(isActive: Boolean) {
        state = state.copy(isActive = isActive)
    }

    fun toggleCreateDialog(show: Boolean) {
        if (!show) {
            resetState()
        } else {
            state = state.copy(showCreateDialog = show)
        }
    }

    fun toggleEditDialog(show: Boolean, categoryWithCount: CategoryWithCount? = null) {
        if (show && categoryWithCount != null) {
            val category = categoryWithCount.category
            state = state.copy(
                showEditDialog = true,
                editingCategoryId = category.id,
                name = category.name,
                emoji = category.emoji,
                displayOrder = category.displayOrder.toString(),
                isActive = category.isActive
            )
        } else {
            resetState()
        }
    }

    fun createCategory() {
        if (!validateForm()) return

        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val result = categoryRepository.insertCategory(
                name = state.name.trim(),
                emoji = state.emoji.trim(),
                displayOrder = state.displayOrder.toIntOrNull() ?: 0
            )

            result.fold(
                onSuccess = {
                    state = CategoryState(successMessage = "Category created successfully!")
                },
                onFailure = { error ->
                    state = state.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to create category"
                    )
                }
            )
        }
    }

    fun updateCategory() {
        if (!validateForm()) return

        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val categoryId = state.editingCategoryId ?: return@launch

            val result = categoryRepository.updateCategory(
                id = categoryId,
                name = state.name.trim(),
                emoji = state.emoji.trim(),
                displayOrder = state.displayOrder.toIntOrNull() ?: 0,
                isActive = state.isActive
            )

            result.fold(
                onSuccess = {
                    state = CategoryState(successMessage = "Category updated successfully!")
                },
                onFailure = { error ->
                    state = state.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to update category"
                    )
                }
            )
        }
    }

    fun deleteCategory(categoryId: Int) {
        viewModelScope.launch {
            val result = categoryRepository.deleteCategory(categoryId)

            result.fold(
                onSuccess = {
                    state = state.copy(successMessage = "Category deleted successfully!")
                },
                onFailure = { error ->
                    state = state.copy(errorMessage = error.message ?: "Failed to delete category")
                }
            )
        }
    }

    fun toggleCategoryStatus(categoryId: Int, isActive: Boolean) {
        viewModelScope.launch {
            val result = categoryRepository.updateCategoryStatus(categoryId, isActive)

            result.fold(
                onSuccess = {
                    // Success - data will auto-update via Flow
                },
                onFailure = { error ->
                    state = state.copy(errorMessage = error.message ?: "Failed to update status")
                }
            )
        }
    }

    fun clearMessages() {
        state = state.copy(successMessage = null, errorMessage = null)
    }

    // ==================== PRIVATE METHODS (Business Logic) ====================

    private fun validateForm(): Boolean {
        var isValid = true

        if (state.name.isBlank()) {
            state = state.copy(nameError = "Category name is required")
            isValid = false
        }

        if (state.emoji.isBlank()) {
            state = state.copy(emojiError = "Emoji is required")
            isValid = false
        }

        val order = state.displayOrder.toIntOrNull()
        if (order == null || order < 0) {
            state = state.copy(displayOrderError = "Display order must be a positive number")
            isValid = false
        }

        return isValid
    }

    private fun resetState() {
        state = CategoryState()
    }
}

// ==================== STATE ====================

data class CategoryState(
    val name: String = "",
    val emoji: String = "",
    val displayOrder: String = "0",
    val isActive: Boolean = true,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingCategoryId: Int? = null,
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emojiError: String? = null,
    val displayOrderError: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

// ==================== DATA MODELS ====================

data class CategoryWithCount(
    val category: Category,
    val menuCount: Int
)
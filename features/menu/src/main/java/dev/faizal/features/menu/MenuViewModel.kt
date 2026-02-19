package dev.faizal.features.menu


import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizal.core.domain.model.menu.Category
import dev.faizal.core.domain.model.menu.Menu
import dev.faizal.core.domain.repository.CategoryRepository
import dev.faizal.core.domain.repository.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    var state by mutableStateOf(MenuState())

    private val _allMenus = MutableStateFlow<List<Menu>>(emptyList())
    private val _categories = MutableStateFlow<List<Category>>(emptyList())

    private val _filteredMenus = MutableStateFlow<List<Menu>>(emptyList())
    val filteredMenus: StateFlow<List<Menu>> = _filteredMenus.asStateFlow()

    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadMenus()
        loadCategories()
        observeFilters()
    }

    // ==================== PUBLIC METHODS ====================

    fun loadMenus() {
        viewModelScope.launch {
            menuRepository.getAllMenus().collect { menuList ->
                _allMenus.value = menuList
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getActiveCategories().collect { categoryList ->
                _categories.value = categoryList
            }
        }
    }

    fun onNameChange(name: String) {
        state = state.copy(name = name, nameError = null)
    }

    fun onCategoryChange(categoryId: Int) {
        state = state.copy(selectedCategoryId = categoryId, categoryError = null)
    }

    fun onPriceChange(price: String) {
        state = state.copy(price = price, priceError = null)
    }

    fun onImageSelected(uri: Uri?) {
        state = state.copy(selectedImageUri = uri)
    }

    fun onIsActiveChange(isActive: Boolean) {
        state = state.copy(isActive = isActive)
    }

    fun onSearchQueryChange(query: String) {
        state = state.copy(searchQuery = query)
    }

    fun onCategoryFilterChange(categoryId: Int?) {
        state = state.copy(selectedFilterCategoryId = categoryId)
    }

    fun toggleCreateDialog(show: Boolean) {
        if (!show) {
            resetFormState()
        } else {
            state = state.copy(showCreateDialog = show)
        }
    }

    fun toggleEditDialog(show: Boolean, menu: Menu? = null) {
        if (show && menu != null) {
            state = state.copy(
                showEditDialog = true,
                editingMenuId = menu.id,
                name = menu.name,
                selectedCategoryId = menu.categoryId,
                price = menu.basePrice.toString(),
                isActive = menu.isActive,
                selectedImageUri = menu.imageUri?.let { Uri.parse(it) }
            )
        } else {
            resetFormState()
        }
    }

    fun createMenu() {
        if (!validateForm()) return

        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val result = menuRepository.insertMenu(
                name = state.name.trim(),
                categoryId = state.selectedCategoryId ?: 0,
                basePrice = state.price.toDouble(),
                imageUri = state.selectedImageUri,
                isActive = state.isActive
            )

            result.fold(
                onSuccess = {
                    state = MenuState(successMessage = "Menu created successfully!")
                },
                onFailure = { error ->
                    state = state.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to create menu"
                    )
                }
            )
        }
    }

    fun updateMenu() {
        if (!validateForm()) return

        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val menuId = state.editingMenuId ?: return@launch

            val result = menuRepository.updateMenu(
                id = menuId,
                name = state.name.trim(),
                categoryId = state.selectedCategoryId ?: 0,
                basePrice = state.price.toDouble(),
                imageUri = state.selectedImageUri,
                isActive = state.isActive
            )

            result.fold(
                onSuccess = {
                    state = MenuState(successMessage = "Menu updated successfully!")
                },
                onFailure = { error ->
                    state = state.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to update menu"
                    )
                }
            )
        }
    }

    fun deleteMenu(menuId: Int) {
        viewModelScope.launch {
            val result = menuRepository.deleteMenu(menuId)

            result.fold(
                onSuccess = {
                    state = state.copy(successMessage = "Menu deleted successfully!")
                },
                onFailure = { error ->
                    state = state.copy(errorMessage = error.message ?: "Failed to delete menu")
                }
            )
        }
    }

    fun toggleMenuStatus(menuId: Int, isActive: Boolean) {
        viewModelScope.launch {
            val result = menuRepository.updateMenuStatus(menuId, isActive)

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

    /**
     * Filter & Search Logic (tanpa UseCase)
     * Similar to FilterMenuItemsUseCase
     */
    private fun observeFilters() {
        viewModelScope.launch {
            combine(
                _allMenus,
                _filteredMenus
            ) { menus, _ ->
                filterMenus(
                    menus = menus,
                    categoryId = state.selectedFilterCategoryId,
                    searchQuery = state.searchQuery
                )
            }.collect { filtered ->
                _filteredMenus.value = filtered
            }
        }
    }

    private fun filterMenus(
        menus: List<Menu>,
        categoryId: Int?,
        searchQuery: String
    ): List<Menu> {
        // Filter by category
        val categoryFiltered = if (categoryId != null) {
            menus.filter { it.categoryId == categoryId }
        } else {
            menus
        }

        // Filter by search query
        return if (searchQuery.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { menu ->
                menu.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    /**
     * Get Best Seller Menus
     * Equivalent to "Best Seller" category filter
     */
    fun getBestSellerMenus(limit: Int = 6): List<Menu> {
        return _allMenus.value
            .sortedByDescending { it.sold }
            .take(limit)
    }

    /**
     * Form Validation
     */
    private fun validateForm(): Boolean {
        var isValid = true

        if (state.name.isBlank()) {
            state = state.copy(nameError = "Menu name is required")
            isValid = false
        }

        if (state.selectedCategoryId == null) {
            state = state.copy(categoryError = "Category is required")
            isValid = false
        }

        if (state.price.isBlank()) {
            state = state.copy(priceError = "Price is required")
            isValid = false
        } else {
            try {
                val price = state.price.toDouble()
                if (price <= 0) {
                    state = state.copy(priceError = "Price must be greater than 0")
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                state = state.copy(priceError = "Invalid price format")
                isValid = false
            }
        }

        return isValid
    }

    private fun resetFormState() {
        state = MenuState(
            searchQuery = state.searchQuery,
            selectedFilterCategoryId = state.selectedFilterCategoryId
        )
    }
}

// ==================== STATE ====================

data class MenuState(
    // Form fields
    val name: String = "",
    val selectedCategoryId: Int? = null,
    val price: String = "",
    val isActive: Boolean = true,
    val selectedImageUri: Uri? = null,

    // Filter fields
    val searchQuery: String = "",
    val selectedFilterCategoryId: Int? = null,

    // Dialog state
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingMenuId: Int? = null,

    // UI state
    val isLoading: Boolean = false,

    // Validation errors
    val nameError: String? = null,
    val categoryError: String? = null,
    val priceError: String? = null,

    // Messages
    val successMessage: String? = null,
    val errorMessage: String? = null
)
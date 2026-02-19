package dev.faizal.favorite

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizal.core.domain.model.report.TopProductReport
import dev.faizal.core.domain.repository.OrderRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class FavoriteProductViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    var state by mutableStateOf(FavoriteProductState())
        private set

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                orderRepository.getTopProductsByMonth(
                    year = state.selectedYear,
                    month = state.selectedMonth,
                    limit = 100 // Get all products
                ).collectLatest { products ->
                    val categories = products.map { it.categoryName }.distinct()
                    state = state.copy(
                        allProducts = products,
                        filteredProducts = products,
                        allCategories = categories,
                        isLoading = false
                    )
                    applyFilters()
                }
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        state = state.copy(searchQuery = query)
        applyFilters()
    }

    fun onMonthYearChanged(month: Int, year: Int) {
        state = state.copy(
            selectedMonth = month,
            selectedYear = year
        )
        loadProducts()
    }

    fun onCategorySelected(category: String?) {
        state = state.copy(selectedCategory = category)
        applyFilters()
    }

    fun toggleSortOrder() {
        state = state.copy(
            sortBy = if (state.sortBy == SortBy.ORDERS) SortBy.REVENUE else SortBy.ORDERS
        )
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = state.allProducts

        // Filter by search query
        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.menuName.contains(state.searchQuery, ignoreCase = true) ||
                        it.categoryName.contains(state.searchQuery, ignoreCase = true)
            }
        }

        // Filter by category
        if (state.selectedCategory != null) {
            filtered = filtered.filter { it.categoryName == state.selectedCategory }
        }

        // Sort
        filtered = when (state.sortBy) {
            SortBy.ORDERS -> filtered.sortedByDescending { it.orderCount }
            SortBy.REVENUE -> filtered.sortedByDescending { it.totalAmount }
        }

        state = state.copy(filteredProducts = filtered)
    }
}

enum class SortBy {
    ORDERS, REVENUE
}
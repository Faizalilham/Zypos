package dev.faizal.favorite

import dev.faizal.core.domain.model.report.TopProductReport
import java.time.LocalDate


data class FavoriteProductState(
    val allProducts: List<TopProductReport> = emptyList(),
    val filteredProducts: List<TopProductReport> = emptyList(),
    val allCategories: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val selectedYear: Int = LocalDate.now().year,
    val sortBy: SortBy = SortBy.ORDERS,
    val isLoading: Boolean = false,
    val error: String? = null
)
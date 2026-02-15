package dev.faizal.zypos.ui.screens.transaction

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizal.zypos.domain.model.order.OrderDetail
import dev.faizal.zypos.domain.model.report.DailySalesReport
import dev.faizal.zypos.domain.repository.OrderRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    var state by mutableStateOf(TransactionState())
        private set

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                // ✅ Gunakan Flow dari repository
                orderRepository.getDailySalesByMonth(
                    year = state.selectedYear,
                    month = state.selectedMonth
                ).collectLatest { dailySales ->
                    state = state.copy(
                        allTransactions = dailySales,
                        filteredTransactions = dailySales,
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

    fun onDateRangeChanged(startDate: String?, endDate: String?) {
        state = state.copy(
            startDate = startDate,
            endDate = endDate
        )
        applyFilters()
    }

    fun onMonthYearChanged(month: Int, year: Int) {
        state = state.copy(
            selectedMonth = month,
            selectedYear = year
        )
        loadTransactions()
    }

    fun onSortByChanged(sortBy: SortBy) {
        state = state.copy(sortBy = sortBy)
        applyFilters()
    }

    fun onSortOrderChanged(ascending: Boolean) {
        state = state.copy(isAscending = ascending)
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = state.allTransactions

        // Filter by search query
        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.date.contains(state.searchQuery, ignoreCase = true) ||
                        it.dayOfMonth.toString().contains(state.searchQuery)
            }
        }

        // Filter by date range
        if (state.startDate != null && state.endDate != null) {
            filtered = filtered.filter { transaction ->
                val transactionDate = LocalDate.parse(transaction.date)
                val start = LocalDate.parse(state.startDate)
                val end = LocalDate.parse(state.endDate)
                !transactionDate.isBefore(start) && !transactionDate.isAfter(end)
            }
        }

        // Sort
        filtered = when (state.sortBy) {
            SortBy.DATE -> {
                if (state.isAscending) filtered.sortedBy { it.dayOfMonth }
                else filtered.sortedByDescending { it.dayOfMonth }
            }
            SortBy.AMOUNT -> {
                if (state.isAscending) filtered.sortedBy { it.totalAmount }
                else filtered.sortedByDescending { it.totalAmount }
            }
            SortBy.ORDERS -> {
                if (state.isAscending) filtered.sortedBy { it.orderCount }
                else filtered.sortedByDescending { it.orderCount }
            }
        }

        state = state.copy(filteredTransactions = filtered)
    }

    fun loadOrdersForDate(date: String) {
        viewModelScope.launch {
            state = state.copy(selectedDate = date, showOrderDialog = true)
            orderRepository.getOrdersByDate(date).collectLatest { orders ->
                state = state.copy(selectedDateOrders = orders)
            }
        }
    }

    fun dismissOrderDialog() {
        state = state.copy(
            showOrderDialog = false,
            selectedDate = null,
            selectedDateOrders = emptyList()
        )
    }

    fun exportToExcel() {
        // TODO: Implement export to Excel
        viewModelScope.launch {
            // Export logic here
        }
    }

    fun exportToPdf() {
        // TODO: Implement export to PDF
        viewModelScope.launch {
            // Export logic here
        }
    }
}

data class TransactionState(
    val allTransactions: List<DailySalesReport> = emptyList(),
    val filteredTransactions: List<DailySalesReport> = emptyList(),
    val searchQuery: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val selectedYear: Int = LocalDate.now().year,
    val sortBy: SortBy = SortBy.DATE,
    val isAscending: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDateOrders: List<OrderDetail> = emptyList(),
    val showOrderDialog: Boolean = false,
    val selectedDate: String? = null
)

enum class SortBy {
    DATE, AMOUNT, ORDERS
}
package dev.faizal.transaction

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.faizal.core.common.model.SortBy
import dev.faizal.core.common.pdf.PdfDownloadHelper
import dev.faizal.core.common.pdf.PdfReportGenerator
import dev.faizal.core.domain.model.order.OrderDetail
import dev.faizal.core.domain.model.report.DailySalesReport
import dev.faizal.core.domain.repository.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var state by mutableStateOf(TransactionState())
        private set

    private val pdfGenerator = PdfReportGenerator(context)
    private val downloadHelper = PdfDownloadHelper(context)

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
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

    fun exportToPdf(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                state = state.copy(isExporting = true)

                // Generate PDF in background
                val result = withContext(Dispatchers.IO) {
                    // Create temp file first
                    val tempFile = File(context.cacheDir, "temp_report.pdf")

                    // Get report data
                    val report = orderRepository.getCompleteMonthlyReport(
                        year = state.selectedYear,
                        month = state.selectedMonth
                    )

                    // Generate PDF
                    pdfGenerator.generateReport(
                        outputFile = tempFile,
                        year = state.selectedYear,
                        month = state.selectedMonth,
                        report = report
                    )

                    tempFile
                }

                // Save to Downloads
                val fileName = downloadHelper.generateFileName(
                    "ZYPOS_Report_${getMonthName(state.selectedMonth)}_${state.selectedYear}"
                )

                downloadHelper.savePdfToDownloads(
                    sourceFile = result,
                    fileName = fileName,
                    onSuccess = { uri ->
                        state = state.copy(isExporting = false)
                        onSuccess()
                    },
                    onError = { exception ->
                        state = state.copy(isExporting = false)
                        onError(exception.message ?: "Failed to save PDF")
                    }
                )

            } catch (e: Exception) {
                state = state.copy(isExporting = false)
                onError(e.message ?: "Failed to generate PDF")
            }
        }
    }

    /**
     * Export and open PDF
     */
    fun exportAndOpenPdf(
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                state = state.copy(isExporting = true)

                val result = withContext(Dispatchers.IO) {
                    val tempFile = File(context.cacheDir, "report_preview.pdf")

                    val report = orderRepository.getCompleteMonthlyReport(
                        year = state.selectedYear,
                        month = state.selectedMonth
                    )

                    pdfGenerator.generateReport(
                        outputFile = tempFile,
                        year = state.selectedYear,
                        month = state.selectedMonth,
                        report = report
                    )

                    tempFile
                }

                state = state.copy(isExporting = false)
                downloadHelper.openPdf(result)

            } catch (e: Exception) {
                state = state.copy(isExporting = false)
                onError(e.message ?: "Failed to open PDF")
            }
        }
    }

    /**
     * Export and share PDF
     */
    fun exportAndSharePdf(
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                state = state.copy(isExporting = true)

                val result = withContext(Dispatchers.IO) {
                    val tempFile = File(context.cacheDir, "report_share.pdf")

                    val report = orderRepository.getCompleteMonthlyReport(
                        year = state.selectedYear,
                        month = state.selectedMonth
                    )

                    pdfGenerator.generateReport(
                        outputFile = tempFile,
                        year = state.selectedYear,
                        month = state.selectedMonth,
                        report = report
                    )

                    tempFile
                }

                state = state.copy(isExporting = false)
                downloadHelper.sharePdf(result)

            } catch (e: Exception) {
                state = state.copy(isExporting = false)
                onError(e.message ?: "Failed to share PDF")
            }
        }
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "Januari"; 2 -> "Februari"; 3 -> "Maret"; 4 -> "April"
            5 -> "Mei"; 6 -> "Juni"; 7 -> "Juli"; 8 -> "Agustus"
            9 -> "September"; 10 -> "Oktober"; 11 -> "November"; 12 -> "Desember"
            else -> ""
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
    val selectedDate: String? = null,
    val isExporting: Boolean = false
)
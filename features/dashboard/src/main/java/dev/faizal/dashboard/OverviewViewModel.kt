package dev.faizal.dashboard


import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizal.core.domain.repository.OrderRepository
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    var state by mutableStateOf(OverviewState())
        private set

    init {
        loadCurrentMonthReport()
    }

    fun loadCurrentMonthReport() {
        val calendar = Calendar.getInstance()
        loadReport(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH) + 1
        )
    }

    fun loadReport(year: Int, month: Int) {
        state = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val report = orderRepository.getCompleteMonthlyReport(year, month)

                state = state.copy(
                    year = year,
                    month = month,
                    report = report,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load report"
                )
            }
        }
    }

    fun updateMonthYear(month: Int, year: Int) {
        // Update state dan fetch data baru
        loadReport(year = year, month = month)
    }

    fun downloadPdfReport(context: Context, onSuccess: (File) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val fileName = "Sales_Report_${state.year}_${String.format("%02d", state.month)}.pdf"
                val outputFile = File(context.getExternalFilesDir(null), fileName)

                val result = orderRepository.generateMonthlyReportPdf(
                    year = state.year,
                    month = state.month,
                    outputFile = outputFile
                )

                result.fold(
                    onSuccess = { file -> onSuccess(file) },
                    onFailure = { exception -> onError(exception.message ?: "Failed to generate PDF") }
                )
            } catch (e: Exception) {
                onError(e.message ?: "Failed to generate PDF")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        state = state.copy(searchQuery = query)
    }
}
package dev.faizal.dashboard

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizal.core.domain.model.store.Store
import dev.faizal.core.domain.repository.OrderRepository
import dev.faizal.core.domain.repository.StoreRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    storeSettingsRepository: StoreRepository,
) : ViewModel() {

    var state by mutableStateOf(OverviewState())
        private set

    /** Expose ke screen untuk personalisasi header (nama toko). */
    val storeSettings: StateFlow<Store?> =
        storeSettingsRepository.observeSettings().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    init {
        loadCurrentMonthReport()
    }

    fun loadCurrentMonthReport() {
        val calendar = Calendar.getInstance()
        loadReport(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH) + 1,
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
                    error = null,
                )
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load report",
                )
            }
        }
    }

    fun updateMonthYear(month: Int, year: Int) {
        loadReport(year = year, month = month)
    }

    fun downloadPdfReport(
        context: Context,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                // Ambil store name untuk filename PDF
                val storeName = storeSettings.value?.storeName ?: "ZyPos"
                val safeStoreName = storeName.replace(Regex("[^a-zA-Z0-9]"), "_")
                val fileName = "${safeStoreName}_Report_${state.year}_${String.format("%02d", state.month)}.pdf"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                    uri?.let {
                        resolver.openOutputStream(it)?.use { outputStream ->
                            val tempFile = File(context.cacheDir, fileName)

                            val result = orderRepository.generateMonthlyReportPdf(
                                year = state.year,
                                month = state.month,
                                outputFile = tempFile,
                            )

                            result.fold(
                                onSuccess = { file ->
                                    outputStream.write(file.readBytes())
                                    contentValues.clear()
                                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                                    resolver.update(uri, contentValues, null, null)
                                    onSuccess(file)
                                },
                                onFailure = { exception ->
                                    resolver.delete(uri, null, null)
                                    onError(exception.message ?: "Failed to generate PDF")
                                },
                            )
                        }
                    } ?: onError("Failed to create file")
                } else {
                    val outputFile = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        fileName,
                    )

                    val result = orderRepository.generateMonthlyReportPdf(
                        year = state.year,
                        month = state.month,
                        outputFile = outputFile,
                    )

                    result.fold(
                        onSuccess = { file -> onSuccess(file) },
                        onFailure = { exception -> onError(exception.message ?: "Failed to generate PDF") },
                    )
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to generate PDF")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        state = state.copy(searchQuery = query)
    }
}
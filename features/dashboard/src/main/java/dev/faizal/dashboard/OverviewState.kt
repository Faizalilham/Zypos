package dev.faizal.dashboard

import dev.faizal.core.domain.model.report.CompleteMonthlyReport
import java.util.Calendar

data class OverviewState(
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val report: CompleteMonthlyReport? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

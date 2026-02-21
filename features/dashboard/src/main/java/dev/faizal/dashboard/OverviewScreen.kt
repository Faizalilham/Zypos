package dev.faizal.dashboard

import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.faizal.core.common.utils.ScreenConfig
import dev.faizal.core.designsystem.*
import dev.faizal.dashboard.component.AllOrdersCard
import dev.faizal.dashboard.component.DatePeriodCard
import dev.faizal.dashboard.component.FavoriteProductCard
import dev.faizal.dashboard.component.GraphCard
import dev.faizal.dashboard.component.StatCard
import dev.faizal.ui.component.Header
import java.text.NumberFormat
import java.util.Locale
import dev.faizal.core.common.utils.*


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    screenConfig: ScreenConfig,
    isDarkMode: Boolean = false,
    onToggleSidebar: () -> Unit = {},
    onNavigationFavorite : () -> Unit = {},
    onNavigationDailySales : () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val reportViewModel: ReportViewModel = hiltViewModel()
    val reportState = reportViewModel.state
    val report = reportState.report

    var selectedMonth by remember { mutableIntStateOf(reportState.month) }
    var selectedYear by remember { mutableIntStateOf(reportState.year) }

    LaunchedEffect(selectedMonth, selectedYear) {
        reportViewModel.updateMonthYear(selectedMonth, selectedYear)
    }

    val totalSales = report?.monthlySales?.totalAmount ?: 0.0
    val netProfit = report?.monthlySales?.netProfit ?: 0.0

    val growthAmount = report?.growth?.growthAmount ?: 0.0
    val growthPercentage = report?.growth?.growthPercentage ?: 0.0

    val chartDataPoints = remember(report?.dailySales) {
        val points = report?.dailySales?.map {
            Pair(it.dayOfMonth.toString(), it.totalAmount.toFloat())
        } ?: emptyList()

        Log.d("ChartDebug", "Report: $report")
        Log.d("ChartDebug", "Daily Sales: ${report?.dailySales}")
        Log.d("ChartDebug", "Chart Data Points: $points")
        Log.d("ChartDebug", "Points size: ${points.size}")

        points
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    horizontal = if (screenConfig.isPhone) 16.dp else 24.dp,
                    vertical = 16.dp
                )
        ) {
            if (screenConfig.isPhone) {
                Text(
                    text = "Report 📊",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                Header(
                    title = "Let's Do Your Best Today",
                    subtitle = getCurrentDateInIndonesian(),
                    emoji = "📦",
                    searchQuery = reportState.searchQuery,
                    onSearchChange = { reportViewModel.onSearchQueryChanged(it) },
                    onMenuClick = onToggleSidebar,
                    isTabletPortrait = screenConfig.isTabletPortrait
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (screenConfig.isPhone) 12.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(if (screenConfig.isPhone) 12.dp else 16.dp)
        ) {

            item {
                DatePeriodCard(
                    isPhone = screenConfig.isPhone,
                    year = selectedYear,
                    month = selectedMonth,
                    onMonthYearChange = { month, year ->
                        selectedMonth = month
                        selectedYear = year
                    },
                    onDownloadClick = {}
                )
            }


            item {
                when {
                    screenConfig.isPhone -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(
                                title = "Total Laba",
                                value = netProfit.toCurrencyString(),
                                unit = "",
                                backgroundColor = if (isDarkMode) CardGreenDark else CardGreenLight,
                                image = R.drawable.best,
                            )
                            StatCard(
                                title = "Total Pendapatan",
                                value = totalSales.toCurrencyString(), // Contoh
                                unit = "",
                                backgroundColor = if (isDarkMode) CardBlueDark else CardBlueLight,
                                image = R.drawable.trend_up,
                            )
                            StatCard(
                                title = "Total Pengeluaran",
                                value = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(totalSales - netProfit)}",
                                unit = "",
                                backgroundColor = if (isDarkMode) CardYellowDark else CardYellowLight,
                                image = R.drawable.trend_down,
                            )
                        }
                    }
                    isLandscape -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Total Laba",
                                value = netProfit.toCurrencyString(),
                                unit = "",
                                backgroundColor = if (isDarkMode) CardGreenDark else CardGreenLight,
                                image = R.drawable.best,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Total Pendapatan",
                                value = totalSales.toCurrencyString(),
                                unit = "",
                                backgroundColor = if (isDarkMode) CardBlueDark else CardBlueLight,
                                image = R.drawable.trend_up,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Total Pengeluaran",
                                value = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(totalSales - netProfit)}",
                                unit = "",
                                backgroundColor = if (isDarkMode) CardYellowDark else CardYellowLight,
                                image = R.drawable.trend_down,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    else -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Total Laba",
                                value = netProfit.toCurrencyString(),
                                unit = "",
                                backgroundColor = if (isDarkMode) CardGreenDark else CardGreenLight,
                                image = R.drawable.best,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Total Pendapatan",
                                value = totalSales.toCurrencyString(),
                                unit = "",
                                backgroundColor = if (isDarkMode) CardBlueDark else CardBlueLight,
                                image = R.drawable.trend_up,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Total Pengeluaran",
                                value = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(totalSales - netProfit)}",
                                unit = "",
                                backgroundColor = if (isDarkMode) CardYellowDark else CardYellowLight,
                                image = R.drawable.trend_down,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Graph and Favorite Product
            item {
                if (screenConfig.isPhone || !isLandscape) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GraphCard(
                            totalAmount = totalSales,
                            growthAmount = growthAmount,
                            growthPercentage = growthPercentage,
                            chartDataPoints = chartDataPoints
                        )
                        FavoriteProductCard(
                            topProducts = report?.topProducts ?: emptyList(),
                            onNavigationFavorite = onNavigationFavorite
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GraphCard(
                            totalAmount = totalSales,
                            growthAmount = growthAmount,
                            growthPercentage = growthPercentage,
                            chartDataPoints = chartDataPoints,
                            modifier = Modifier.weight(1.5f)
                        )
                        FavoriteProductCard(
                            topProducts = report?.topProducts ?: emptyList(),
                            modifier = Modifier.weight(1f),
                            onNavigationFavorite = onNavigationFavorite
                        )
                    }
                }
            }

            item {
                AllOrdersCard(
                    isPhone = screenConfig.isPhone,
                    dailySales = report?.dailySales ?: emptyList(),
                    onNavigation = onNavigationDailySales
                )
            }
        }
    }
}




data class Product(val name: String, val category: String, val orders: Int)



data class Order(
    val id: String,
    val dateTime: String,
    val customerName: String,
    val status: String,
    val payment: String,
    val orderStatus: String
)



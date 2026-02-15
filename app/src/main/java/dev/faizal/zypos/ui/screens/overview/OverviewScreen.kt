package dev.faizal.zypos.ui.screens.overview

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.faizal.zypos.R
import dev.faizal.zypos.domain.model.report.DailySalesReport
import dev.faizal.zypos.domain.model.report.TopProductReport
import dev.faizal.zypos.ui.common.Header
import dev.faizal.zypos.ui.common.MonthYearPickerDialog
import dev.faizal.zypos.ui.common.PieChartLegend
import dev.faizal.zypos.ui.common.SimplePieChart
import dev.faizal.zypos.ui.theme.AccentGreen
import dev.faizal.zypos.ui.theme.CardBlueDark
import dev.faizal.zypos.ui.theme.CardBlueLight
import dev.faizal.zypos.ui.theme.CardGreenDark
import dev.faizal.zypos.ui.theme.CardGreenLight
import dev.faizal.zypos.ui.theme.CardYellowDark
import dev.faizal.zypos.ui.theme.CardYellowLight
import dev.faizal.zypos.ui.utils.ScreenConfig
import java.text.NumberFormat
import java.util.Locale

// ui/screens/overview/ReportScreen.kt
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
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // ✅ Tambahkan ReportViewModel
    val reportViewModel: ReportViewModel = hiltViewModel()
    val reportState = reportViewModel.state
    val report = reportState.report

    var selectedMonth by remember { mutableStateOf(reportState.month) }
    var selectedYear by remember { mutableStateOf(reportState.year) }

    // ✅ Sync state dengan reportViewModel
    LaunchedEffect(selectedMonth, selectedYear) {
        reportViewModel.updateMonthYear(selectedMonth, selectedYear)
    }

    // ✅ Extract data dari report atau gunakan default 0
    val totalSales = report?.monthlySales?.totalAmount ?: 0.0
    val netProfit = report?.monthlySales?.netProfit ?: 0.0

    val growthAmount = report?.growth?.growthAmount ?: 0.0
    val growthPercentage = report?.growth?.growthPercentage ?: 0.0

    // ✅ Convert DailySalesReport ke format chart
    val chartDataPoints = remember(report?.dailySales) {
        val points = report?.dailySales?.map {
            Pair(it.dayOfMonth.toString(), it.totalAmount.toFloat())
        } ?: emptyList()

        // ✅ Tambahkan log untuk debug
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
            // Date Period & Download
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

            // Stats Cards
            item {
                when {
                    screenConfig.isPhone -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(
                                title = "Total Laba",
                                value = "Rp ${String.format("%,.0f", netProfit)}",
                                unit = "",
                                backgroundColor = if (isDarkMode) CardGreenDark else CardGreenLight,
                                image = R.drawable.best,
                            )
                            StatCard(
                                title = "Total Pendapatan",
                                value = "Rp ${String.format("%,.0f", totalSales)}", // Contoh
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
                                value = "Rp ${String.format("%,.0f", netProfit)}",
                                unit = "",
                                backgroundColor = if (isDarkMode) CardGreenDark else CardGreenLight,
                                image = R.drawable.best,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Total Pendapatan",
                                value = "Rp ${String.format("%,.0f", totalSales)}",
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
                                value = "Rp ${String.format("%,.0f", netProfit)}",
                                unit = "",
                                backgroundColor = if (isDarkMode) CardGreenDark else CardGreenLight,
                                image = R.drawable.best,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Total Pendapatan",
                                value = "Rp ${String.format("%,.0f", totalSales)}",
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

            // All Orders Section
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

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String,
    backgroundColor: Color,
    image: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f)) // ← Ubah jadi putih transparan
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(image),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Black.copy(alpha = 0.4f) // ← Warna icon hitam transparan
                )
            }

            // Content
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = Color.Black.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = value,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GraphCard(
    totalAmount: Double,
    growthAmount: Double,
    growthPercentage: Double,
    chartDataPoints: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().height(500.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Report Graph",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (chartDataPoints.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painterResource(R.drawable.no_order),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No Data Available",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Start making sales to see the graph",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                WaveChart(
                    dataPoints = chartDataPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoBox("Amount", String.format("%,.2f", totalAmount), "Rp", Modifier.weight(1f))
                InfoBox("Growth", "${if (growthAmount >= 0) "+" else ""} ${String.format("%,.2f", growthAmount)}", "Rp", Modifier.weight(1f))
                InfoBox("Growth Percentage", "${if (growthPercentage >= 0) "↑" else "↓"} ${String.format("%.1f", kotlin.math.abs(growthPercentage))}", "Percent (%)", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun InfoBox(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline, // ✅ Dark mode
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant // ✅ Dark mode
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface // ✅ Dark mode
        )
        Text(
            unit,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant // ✅ Dark mode
        )
    }
}

data class Product(val name: String, val category: String, val orders: Int)

@Composable
fun FavoriteProductCard(
    topProducts: List<TopProductReport>,
    modifier: Modifier = Modifier,
    onNavigationFavorite: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().height(500.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Favorite Product",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                ViewAllButton(
                    onClick = onNavigationFavorite
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (topProducts.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(R.drawable.no_order),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No Products Yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // ✅ Pie Chart Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SimplePieChart(
                        data = topProducts.map { it.menuName to it.orderCount },
                        modifier = Modifier
                    )

                    PieChartLegend(
                        data = topProducts.map { it.menuName to it.orderCount },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Column Headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        color = Color.Transparent
                    ) {
                        Text(
                            "Img",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        color = Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Product Name",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        color = Color.Transparent
                    ) {
                        Text(
                            "Total Orders",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Product List
                topProducts.forEachIndexed { index, product ->
                    TopProductItem(product)
                    if (index < topProducts.size - 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TopProductItem(product: TopProductReport) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!product.imageUri.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Menu Image",
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = null,
                    tint = Color(0xFFBDBDBD),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                product.menuName,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = Color(0xFF00BCD4).copy(alpha = 0.15f)
            ) {
                Text(
                    product.categoryName,
                    fontSize = 11.sp,
                    color = Color(0xFF00BCD4),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            "${product.orderCount} Times",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProductItem(product: Product) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFFF3E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Product Name and Category (weight 1f, center aligned)
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                product.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Category with background
            val (categoryColor, backgroundColor) = when(product.category) {
                "Pastry" -> Color(0xFF00BCD4) to Color(0xFF00BCD4).copy(alpha = 0.15f)
                "Sandwich" -> Color(0xFFFF9800) to Color(0xFFFF9800).copy(alpha = 0.15f)
                "Cake" -> Color(0xFFE91E63) to Color(0xFFE91E63).copy(alpha = 0.15f)
                else -> Color.Gray to Color.Gray.copy(alpha = 0.15f)
            }

            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = backgroundColor
            ) {
                Text(
                    product.category,
                    fontSize = 11.sp,
                    color = categoryColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Total Orders (wrap content)
        Text(
            "${product.orders} Times",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
    }
}

data class Order(
    val id: String,
    val dateTime: String,
    val customerName: String,
    val status: String,
    val payment: String,
    val orderStatus: String
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AllOrdersCard(
    isPhone: Boolean = false,
    dailySales: List<DailySalesReport> = emptyList(),
    onNavigation : () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
        ){
            if (isPhone) {
                // ✅ PHONE: Simplified header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2196F3))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Daily Sales",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    ViewAllButton(
                        onClick = onNavigation
                    )
                }
            } else {
                // ✅ TABLET: Full header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2196F3))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Daily Sales Report",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ViewAllButton(
                            onClick = onNavigation
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isPhone) 4.dp else 8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
        ) {
            if (dailySales.isEmpty()) {
                // ✅ Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Sales Data",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Sales data will appear here",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isPhone) {
                        // ✅ PHONE: Vertical cards
                        dailySales.forEach { sale ->
                            DailySalesCardItem(sale)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    } else {
                        // ✅ TABLET: Table view
                        Spacer(modifier = Modifier.height(16.dp))

                        // Table Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TableHeaderCell("Date", Modifier.weight(1.5f))
                            TableHeaderCell("Day", Modifier.weight(1f))
                            TableHeaderCell("Orders", Modifier.weight(1f))
                            TableHeaderCell("Total Amount", Modifier.weight(1.5f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Table Rows
                        dailySales.forEach { sale ->
                            DailySalesRow(sale)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Text(
            text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DailySalesCardItem(sale: DailySalesReport) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Day ${sale.dayOfMonth}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFE3F2FD)
                ) {
                    Text(
                        "${sale.orderCount} Orders",
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color(0xFF2196F3)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                sale.date,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${String.format("%,.2f", sale.totalAmount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen
                )
            }
        }
    }
}

@Composable
fun DailySalesRow(sale: DailySalesReport) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            sale.date,
            fontSize = 13.sp,
            modifier = Modifier
                .weight(1.5f)
                .padding(6.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Day ${sale.dayOfMonth}",
            fontSize = 13.sp,
            modifier = Modifier
                .weight(1f)
                .padding(6.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            sale.orderCount.toString(),
            fontSize = 13.sp,
            modifier = Modifier
                .weight(1f)
                .padding(6.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Rp ${String.format("%,.2f", sale.totalAmount)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1.5f)
                .padding(6.dp),
            textAlign = TextAlign.Center,
            color = AccentGreen
        )
    }
}

@Composable
fun DatePeriodCard(
    isPhone: Boolean,
    year: Int,
    month: Int,
    onMonthYearChange: (month: Int, year: Int) -> Unit,
    onDownloadClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val monthName = when (month) {
        1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
        5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
        9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
        else -> ""
    }

    if (isPhone) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showDialog = true }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.calendar),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF2196F3)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "$monthName $year",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDownloadClick) {
                    Icon(
                        painter = painterResource(R.drawable.download),
                        contentDescription = "Download",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Date Periode:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.clickable { showDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "$monthName $year",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(44.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2196F3).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.calendar),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF2196F3)
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.clickable { onDownloadClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Download",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2196F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.download),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // ✅ Dialog Popup
    if (showDialog) {
        MonthYearPickerDialog(
            initialMonth = monthName,
            initialYear = year,
            onDismiss = { showDialog = false },
            onConfirm = { selectedMonth, selectedYear ->
                onMonthYearChange(selectedMonth + 1, selectedYear) // +1 karena index dimulai dari 0
                showDialog = false
            }
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WaveChart(
    dataPoints: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    var selectedDataPoint by remember { mutableStateOf<Pair<Int, Float>?>(null) }
    var isHovering by remember { mutableStateOf(false) }

    // ✅ Gunakan dataPoints dari parameter, bukan generate random
    val fullDataPoints = remember(dataPoints) {
        dataPoints.ifEmpty {
            // Default data jika kosong
            List(30) { day ->
                Pair((day + 1).toString(), 0f)
            }
        }
    }

    val itemWidth = 80.dp

    Box(modifier = modifier) {
        if (fullDataPoints.all { it.second == 0f }) {
            // ✅ Empty state jika semua data 0
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No Sales Data",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
            ) {
                Canvas(
                    modifier = Modifier
                        .width(itemWidth * fullDataPoints.size)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    when (event.type) {
                                        PointerEventType.Move, PointerEventType.Enter -> {
                                            isHovering = true
                                            val position = event.changes.first().position
                                            val spacing = size.width / (fullDataPoints.size - 1).coerceAtLeast(1)
                                            val index = ((position.x / spacing).toInt())
                                                .coerceIn(0, fullDataPoints.size - 1)
                                            selectedDataPoint = Pair(index, fullDataPoints[index].second)
                                        }
                                        PointerEventType.Exit -> {
                                            isHovering = false
                                            selectedDataPoint = null
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    if (fullDataPoints.size < 2) return@Canvas

                    val spacing = width / (fullDataPoints.size - 1)

                    val minValue = fullDataPoints.minOf { it.second }
                    val maxValue = fullDataPoints.maxOf { it.second }
                    val valueRange = (maxValue - minValue).coerceAtLeast(1f)

                    val wavePath = Path()
                    val fillPath = Path()

                    fillPath.moveTo(0f, height)

                    val wavePoints = mutableListOf<Offset>()

                    fullDataPoints.forEachIndexed { index, (_, value) ->
                        val x = index * spacing
                        val normalizedValue = (value - minValue) / valueRange
                        val y = height - (normalizedValue * height * 0.7f) - (height * 0.15f)

                        wavePoints.add(Offset(x, y))
                    }

                    // Create smooth curve
                    wavePoints.forEachIndexed { index, point ->
                        if (index == 0) {
                            wavePath.moveTo(point.x, point.y)
                            fillPath.lineTo(point.x, point.y)
                        } else {
                            val prevPoint = wavePoints[index - 1]

                            val p0 = if (index > 1) wavePoints[index - 2] else prevPoint
                            val p1 = prevPoint
                            val p2 = point
                            val p3 = if (index < wavePoints.size - 1) wavePoints[index + 1] else point

                            val tension = 1.0f

                            val controlX1 = p1.x + (p2.x - p0.x) * tension / 6
                            val controlY1 = p1.y + (p2.y - p0.y) * tension / 6
                            val controlX2 = p2.x - (p3.x - p1.x) * tension / 6
                            val controlY2 = p2.y - (p3.y - p1.y) * tension / 6

                            wavePath.cubicTo(
                                controlX1, controlY1,
                                controlX2, controlY2,
                                point.x, point.y
                            )
                            fillPath.cubicTo(
                                controlX1, controlY1,
                                controlX2, controlY2,
                                point.x, point.y
                            )
                        }
                    }

                    fillPath.lineTo(width, height)
                    fillPath.lineTo(0f, height)
                    fillPath.close()

                    // Draw gradient fill
                    drawPath(
                        path = fillPath,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xFF2196F3).copy(alpha = 0.4f),
                                0.3f to Color(0xFF2196F3).copy(alpha = 0.25f),
                                0.6f to Color(0xFF2196F3).copy(alpha = 0.15f),
                                0.8f to Color(0xFF2196F3).copy(alpha = 0.05f),
                                1.0f to Color(0xFF2196F3).copy(alpha = 0.0f)
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Draw wave line with shadow
                    drawPath(
                        path = wavePath,
                        color = Color(0xFF2196F3).copy(alpha = 0.2f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 4.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                    )

                    drawPath(
                        path = wavePath,
                        color = Color(0xFF2196F3),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.5.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                    )

                    // Draw hover effects
                    selectedDataPoint?.let { (selectedIndex, _) ->
                        if (selectedIndex < wavePoints.size) {
                            val point = wavePoints[selectedIndex]

                            drawLine(
                                color = Color(0xFF2196F3).copy(alpha = 0.2f),
                                start = Offset(point.x, 0f),
                                end = Offset(point.x, height),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                    floatArrayOf(8f, 8f)
                                )
                            )

                            drawCircle(
                                color = Color(0xFF2196F3).copy(alpha = 0.2f),
                                radius = 12.dp.toPx(),
                                center = point
                            )

                            drawCircle(
                                color = Color.White,
                                radius = 7.dp.toPx(),
                                center = point
                            )

                            drawCircle(
                                color = Color(0xFF2196F3),
                                radius = 5.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }

            // Tooltip
            if (isHovering) {
                selectedDataPoint?.let { (index, value) ->
                    if (index < fullDataPoints.size) {
                        val point = fullDataPoints[index]

                        val currentMonth = java.time.LocalDate.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("MMMM"))

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                        ) {
                            TooltipPopup(
                                label = "$currentMonth ${point.first}",
                                value = value,
                                modifier = Modifier
                            )
                        }
                    }
                }
            }

            // Scroll indicator
            if (fullDataPoints.size > 10) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1A1A1A).copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_up),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                            Text(
                                "Scroll",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            Icon(
                                painter = painterResource(R.drawable.arrow_down),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TooltipPopup(
    label: String,
    value: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1A1A1A),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rp ${String.format("%,.2f", value)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getCurrentDateInIndonesian(): String {
    val currentDate = java.time.LocalDate.now()
    val dayOfWeek = when (currentDate.dayOfWeek) {
        java.time.DayOfWeek.MONDAY -> "Senin"
        java.time.DayOfWeek.TUESDAY -> "Selasa"
        java.time.DayOfWeek.WEDNESDAY -> "Rabu"
        java.time.DayOfWeek.THURSDAY -> "Kamis"
        java.time.DayOfWeek.FRIDAY -> "Jumat"
        java.time.DayOfWeek.SATURDAY -> "Sabtu"
        java.time.DayOfWeek.SUNDAY -> "Minggu"
    }

    val month = when (currentDate.monthValue) {
        1 -> "Januari"
        2 -> "Februari"
        3 -> "Maret"
        4 -> "April"
        5 -> "Mei"
        6 -> "Juni"
        7 -> "Juli"
        8 -> "Agustus"
        9 -> "September"
        10 -> "Oktober"
        11 -> "November"
        12 -> "Desember"
        else -> ""
    }

    return "$dayOfWeek, ${currentDate.dayOfMonth} $month ${currentDate.year}"
}

@Composable
fun ViewAllButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lihat semua",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal,
            )
            Icon(
                painter = painterResource(R.drawable.external_link), // atau bisa pakai Icons
                contentDescription = "View all",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
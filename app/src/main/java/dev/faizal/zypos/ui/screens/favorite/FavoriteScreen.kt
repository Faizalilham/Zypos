package dev.faizal.zypos.ui.screens.product

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import dev.faizal.zypos.domain.model.report.TopProductReport
import dev.faizal.zypos.ui.common.Header
import dev.faizal.zypos.ui.common.MonthYearPickerDialog
import dev.faizal.zypos.ui.common.PieChartLegend
import dev.faizal.zypos.ui.common.SimplePieChart
import dev.faizal.zypos.ui.screens.favorite.FavoriteProductViewModel
import dev.faizal.zypos.ui.screens.favorite.SortBy
import dev.faizal.zypos.ui.theme.AccentGreen
import dev.faizal.zypos.ui.utils.ScreenConfig

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FavoriteProductDetailScreen(
    screenConfig: ScreenConfig,
    onToggleSidebar: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: FavoriteProductViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showMonthYearDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showDateRangeDialog by remember { mutableStateOf(false) } // ✅ TAMBAH INI
    var selectedProduct by remember { mutableStateOf<TopProductReport?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header - FIXED (tidak ikut scroll)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text(
                        text = "Favorite Products 🏆",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = { showFilterDialog = true }) {
                        Image(
                            painter = painterResource(id = R.drawable.filter),
                            contentDescription = "Filter",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                Header(
                    title = "Favorite Products",
                    subtitle = "Detailed view of your best-selling products",
                    emoji = "🏆",
                    searchQuery = state.searchQuery,
                    onSearchChange = { viewModel.onSearchQueryChanged(it) },
                    onMenuClick = onToggleSidebar,
                    isTabletPortrait = screenConfig.isTabletPortrait,
                )
            }
        }

        // ✅ SCROLLABLE CONTENT
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Action Bar
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        FilterChip(
                            selected = false,
                            onClick = { showMonthYearDialog = true },
                            label = {
                                Text(
                                    "${getMonthName(state.selectedMonth)} ${state.selectedYear}",
                                    fontSize = 13.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.calendar),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )

                        FilterChip(
                            selected = state.selectedCategory != null,
                            onClick = { showFilterDialog = true },
                            label = {
                                Text(
                                    state.selectedCategory ?: "All Categories",
                                    fontSize = 13.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.filter),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Sort Options
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.toggleSortOrder() },
                            label = {
                                Text(
                                    if (state.sortBy == SortBy.ORDERS) "Orders" else "Revenue",
                                    fontSize = 13.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.sort),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Summary Statistics
            item {
                ProductSummaryStats(
                    products = state.filteredProducts,
                    isPhone = screenConfig.isPhone
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Charts Section
            if (state.filteredProducts.isNotEmpty()) {
                item {
                    ProductChartsSection(
                        products = state.filteredProducts,
                        isPhone = screenConfig.isPhone
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Loading or Empty State
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.filteredProducts.isEmpty()) {
                item {
                    EmptyProductState()
                }
            } else {
                // Product List
                items(state.filteredProducts) { product ->
                    DetailedProductCard(
                        product = product,
                        onClick = { selectedProduct = product }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    // Dialogs
    if (showMonthYearDialog) {
        MonthYearPickerDialog(
            initialMonth = getMonthName(state.selectedMonth),
            initialYear = state.selectedYear,
            onDismiss = { showMonthYearDialog = false },
            onConfirm = { month, year ->
                viewModel.onMonthYearChanged(month + 1, year)
                showMonthYearDialog = false
            }
        )
    }

    if (showFilterDialog) {
        CategoryFilterDialog(
            categories = state.allCategories,
            selectedCategory = state.selectedCategory,
            onDismiss = { showFilterDialog = false },
            onSelect = { category ->
                viewModel.onCategorySelected(category)
                showFilterDialog = false
            }
        )
    }

    // Product Detail Bottom Sheet
    if (selectedProduct != null) {
        ProductDetailBottomSheet(
            product = selectedProduct!!,
            onDismiss = { selectedProduct = null }
        )
    }
}

@Composable
fun ProductChartsSection(
    products: List<TopProductReport>,
    isPhone: Boolean
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Sales Distribution", "Top 10 Orders", "Revenue Comparison")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Content
            when (selectedTab) {
                0 -> PieChartSection(products = products.take(5))
                1 -> BarChartSection(products = products.take(10), type = "orders")
                2 -> BarChartSection(products = products.take(10), type = "revenue")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PieChartSection(products: List<TopProductReport>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Top 5 Products by Orders",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pie Chart
            SimplePieChart(
                data = products.map { it.menuName to it.orderCount },
                modifier = Modifier.size(160.dp)
            )

            // Legend
            PieChartLegend(
                data = products.map { it.menuName to it.orderCount },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BarChartSection(
    products: List<TopProductReport>,
    type: String // "orders" or "revenue"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = if (type == "orders") "Top 10 by Orders" else "Top 10 by Revenue",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val maxValue = if (type == "orders") {
            products.maxOfOrNull { it.orderCount }?.toDouble() ?: 1.0
        } else {
            products.maxOfOrNull { it.totalAmount } ?: 1.0
        }

        products.forEachIndexed { index, product ->
            HorizontalBarItem(
                rank = index + 1,
                name = product.menuName,
                value = if (type == "orders") product.orderCount.toDouble() else product.totalAmount,
                maxValue = maxValue,
                type = type,
                color = getBarColor(index)
            )

            if (index < products.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun HorizontalBarItem(
    rank: Int,
    name: String,
    value: Double,
    maxValue: Double,
    type: String,
    color: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Rank Badge
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when (rank) {
                                1 -> Color(0xFFFFD700) // Gold
                                2 -> Color(0xFFC0C0C0) // Silver
                                3 -> Color(0xFFCD7F32) // Bronze
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = name,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = if (type == "orders") {
                    "${value.toInt()} orders"
                } else {
                    "Rp ${String.format("%,.0f", value)}"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (value / maxValue).toFloat())
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

fun getBarColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFFC107), // Amber
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF00BCD4), // Cyan
        Color(0xFF8BC34A), // Light Green
        Color(0xFFFF9800), // Orange
        Color(0xFF607D8B)  // Blue Grey
    )
    return colors[index % colors.size]
}

@Composable
fun ProductSummaryStats(
    products: List<TopProductReport>,
    isPhone: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        if (isPhone) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProductStatItem("Total Products", products.size.toString())
                ProductStatItem(
                    "Total Orders",
                    products.sumOf { it.orderCount }.toString()
                )
                ProductStatItem(
                    "Total Revenue",
                    "Rp ${String.format("%,.0f", products.sumOf { it.totalAmount })}"
                )
                ProductStatItem(
                    "Average Orders",
                    "${products.map { it.orderCount }.average().toInt()}"
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProductStatItem("Total Products", products.size.toString())
                VerticalDivider(modifier = Modifier.height(40.dp))
                ProductStatItem(
                    "Total Orders",
                    products.sumOf { it.orderCount }.toString()
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                ProductStatItem(
                    "Total Revenue",
                    "Rp ${String.format("%,.0f", products.sumOf { it.totalAmount })}"
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                ProductStatItem(
                    "Average Orders",
                    "${products.map { it.orderCount }.average().toInt()}"
                )
            }
        }
    }
}

@Composable
fun ProductStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun DetailedProductCard(
    product: TopProductReport,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
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
            }

            // Product Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.menuName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = product.categoryName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // Orders Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF2196F3).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFF2196F3)
                            )
                            Text(
                                text = "${product.orderCount} orders",
                                fontSize = 11.sp,
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Revenue Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AccentGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Rp ${String.format("%,.0f", product.totalAmount)}",
                            fontSize = 11.sp,
                            color = AccentGreen,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Arrow Icon
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailBottomSheet(
    product: TopProductReport,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.snack),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column {
                    Text(
                        text = product.menuName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = product.categoryName,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetailStatItem(
                    label = "Total Orders",
                    value = product.orderCount.toString(),
                    image = R.drawable.order_list_outlined,
                    color = Color(0xFF2196F3)
                )
                DetailStatItem(
                    label = "Total Revenue",
                    value = "Rp ${String.format("%,.0f", product.totalAmount)}",
                    image = R.drawable.ic_transaction,
                    color = AccentGreen
                )
                DetailStatItem(
                    label = "Avg per Order",
                    value = "Rp ${String.format("%,.0f", product.totalAmount / product.orderCount)}",
                    image = R.drawable.trend_up,
                    color = Color(0xFFFFC107)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DetailStatItem(
    label: String,
    value: String,
    image : Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CategoryFilterDialog(
    categories: List<String>,
    selectedCategory: String?,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Category") },
        text = {
            LazyColumn {
                item {
                    FilterCategoryItem(
                        category = "All Categories",
                        isSelected = selectedCategory == null,
                        onClick = { onSelect(null) }
                    )
                }
                items(categories) { category ->
                    FilterCategoryItem(
                        category = category,
                        isSelected = category == selectedCategory,
                        onClick = { onSelect(category) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun FilterCategoryItem(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category,
            fontSize = 14.sp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptyProductState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.no_order),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Text(
                "No Products Found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Try adjusting your filters",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
        5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
        9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
        else -> ""
    }
}
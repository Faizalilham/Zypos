package dev.faizal.favorite

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.faizal.core.common.utils.ScreenConfig
import dev.faizal.core.common.utils.getMonthName
import dev.faizal.core.designsystem.R
import dev.faizal.core.domain.model.report.TopProductReport
import dev.faizal.favorite.component.DetailedProductCard
import dev.faizal.favorite.component.ProductChartsSection
import dev.faizal.favorite.component.ProductDetailBottomSheet
import dev.faizal.favorite.component.ProductSummaryStats
import dev.faizal.ui.component.CategoryFilterDialog
import dev.faizal.ui.component.EmptyState
import dev.faizal.ui.component.Header
import dev.faizal.ui.component.MonthYearPickerDialog

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
                    EmptyState(
                        imageRes = R.drawable.no_order,
                        title = "No Products Found",
                        subtitle = "Try adjusting your filters"
                    )
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
package dev.faizal.transaction

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import dev.faizal.core.common.utils.ScreenConfig
import dev.faizal.core.common.utils.formatDateToIndonesian
import dev.faizal.core.common.utils.getMonthName
import dev.faizal.core.common.utils.toCurrencyString
import dev.faizal.core.designsystem.R
import dev.faizal.transaction.component.EnhancedOrderMenuItem
import dev.faizal.transaction.component.StatItem
import dev.faizal.transaction.component.TransactionCard
import dev.faizal.transaction.component.TransactionSummaryCard
import dev.faizal.transaction.component.TransactionTableHeader
import dev.faizal.transaction.component.TransactionTableRow
import dev.faizal.ui.component.EmptyState
import dev.faizal.ui.component.ExportDialog
import dev.faizal.ui.component.FilterDialog
import dev.faizal.ui.component.Header
import dev.faizal.ui.component.MonthYearPickerDialog
import dev.faizal.ui.component.SortDialog

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionAllScreen(
    screenConfig: ScreenConfig,
    onToggleSidebar: () -> Unit = {},
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showMonthYearDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Transactions 💰",
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
                    title = "All Transactions",
                    subtitle = "Manage and view all your transactions",
                    emoji = "💰",
                    searchQuery = state.searchQuery,
                    onSearchChange = { viewModel.onSearchQueryChanged(it) },
                    onMenuClick = onToggleSidebar,
                    isTabletPortrait = screenConfig.isTabletPortrait
                )
            }
        }

        // Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
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
                // Month Year Picker
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

                // Filter Button
                FilterChip(
                    selected = state.startDate != null || state.endDate != null,
                    onClick = { showFilterDialog = true },
                    label = { Text("Filter", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.filter),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (state.startDate != null || state.endDate != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )


                // Sort Button
                FilterChip(
                    selected = false,
                    onClick = { showSortDialog = true },
                    label = { Text("Sort", fontSize  = 13.sp) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.sort),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                // Export Button
                if (!screenConfig.isPhone) {
                    Button(
                        onClick = { showExportDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.download),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export", fontSize = 13.sp)
                    }
                } else {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.download),
                            contentDescription = "Export",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Statistics Summary
        TransactionSummaryCard(
            totalTransactions = state.filteredTransactions.size,
            totalAmount = state.filteredTransactions.sumOf { it.totalAmount },
            totalOrders = state.filteredTransactions.sumOf { it.orderCount },
            isPhone = screenConfig.isPhone
        )

        // Transaction List
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.filteredTransactions.isEmpty()) {
            EmptyState(
                imageRes = R.drawable.no_order,
                title = "No Transactions Found",
                subtitle = "Try adjusting your filters"
            )
        } else {
            if (screenConfig.isPhone) {
                // Phone: Card Layout
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredTransactions) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            onDateClick = { date ->
                                viewModel.loadOrdersForDate(date)
                            }
                        )
                    }
                }
            } else {
                // Tablet: Table Layout
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Table Header
                        item {
                            TransactionTableHeader()
                        }

                        // Table Rows
                        items(state.filteredTransactions) { transaction ->
                            TransactionTableRow(
                                transaction = transaction,
                                onDateClick = { date ->
                                    viewModel.loadOrdersForDate(date)
                                },
                                onExportPdf = {},
                                onExportExcel = {}
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 0.5.dp
                            )
                        }
                    }
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
        FilterDialog(
            startDate = state.startDate,
            endDate = state.endDate,
            onDismiss = { showFilterDialog = false },
            onApply = { start, end ->
                viewModel.onDateRangeChanged(start, end)
                showFilterDialog = false
            },
            onClear = {
                viewModel.onDateRangeChanged(null, null)
                showFilterDialog = false
            }
        )
    }

    if (showSortDialog) {
        SortDialog(
            currentSortBy = state.sortBy,
            isAscending = state.isAscending,
            onDismiss = { showSortDialog = false },
            onApply = { sortBy, ascending ->
                viewModel.onSortByChanged(sortBy)
                viewModel.onSortOrderChanged(ascending)
                showSortDialog = false
            }
        )
    }

    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExportExcel = {
                viewModel.exportToExcel()
                showExportDialog = false
            },
            onExportPdf = {
                viewModel.exportToPdf()
                showExportDialog = false
            }
        )
    }

    if (state.showOrderDialog && state.selectedDate != null) {
        Dialog(onDismissRequest = { viewModel.dismissOrderDialog() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header dengan gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Menu Terjual",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatDateToIndonesian(state.selectedDate!!),
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.dismissOrderDialog() }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Summary Stats
                    if (state.selectedDateOrders.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(
                                    label = "Total Menu",
                                    value = state.selectedDateOrders.size.toString(),
                                    icon = Icons.Default.ShoppingCart
                                )
                                VerticalDivider(
                                    modifier = Modifier.height(40.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                StatItem(
                                    label = "Total Qty",
                                    value = state.selectedDateOrders.sumOf { it.quantity }.toString(),
                                    icon = Icons.Default.ShoppingCart
                                )
                                VerticalDivider(
                                    modifier = Modifier.height(40.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                StatItem(
                                    label = "Total",
                                    value = state.selectedDateOrders.sumOf { it.totalPrice }.toCurrencyString(),
                                    icon = Icons.Default.Menu
                                )
                            }
                        }
                    }

                    // Content
                    if (state.selectedDateOrders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Memuat data...",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(state.selectedDateOrders) { order ->
                                EnhancedOrderMenuItem(order)
                            }
                        }
                    }
                }
            }
        }
    }
}
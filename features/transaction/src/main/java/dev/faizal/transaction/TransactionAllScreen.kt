package dev.faizal.transaction

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header — hanya tampil di tablet
        if (!screenConfig.isPhone) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = if (screenConfig.isPhone) 8.dp else 0.dp),
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
                        fontSize = if (screenConfig.isPhone) 12.sp else 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.calendar),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            )

            // Filter Button — selalu tampil
            FilterChip(
                selected = state.startDate != null || state.endDate != null,
                onClick = { showFilterDialog = true },
                label = {
                    Text(
                        "Filter",
                        fontSize = if (screenConfig.isPhone) 12.sp else 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.filter),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (state.startDate != null || state.endDate != null)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            // Sort & Export hanya di tablet
            if (!screenConfig.isPhone) {
                FilterChip(
                    selected = false,
                    onClick = { showSortDialog = true },
                    label = { Text("Sort", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.sort),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

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
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                    Text("Export", fontSize = 13.sp)
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.filteredTransactions) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            onDateClick = { date -> viewModel.loadOrdersForDate(date) }
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { TransactionTableHeader() }
                        items(state.filteredTransactions) { transaction ->
                            TransactionTableRow(
                                transaction = transaction,
                                onDateClick = { date -> viewModel.loadOrdersForDate(date) },
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

    // Di TransactionAllScreen
    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExportExcel = {
                viewModel.exportToExcel()
            },
            onDownloadPdf = {
                viewModel.exportToPdf(
                    onSuccess = {
                        Toast.makeText(context, "PDF downloaded successfully", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Failed to download PDF: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onSharePdf = {
                viewModel.exportAndSharePdf(
                    onError = { error ->
                        Toast.makeText(context, "Failed to share PDF: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
    if (state.isExporting) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    if (state.showOrderDialog && state.selectedDate != null) {
        Dialog(onDismissRequest = { viewModel.dismissOrderDialog() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
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
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Menu Terjual",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = formatDateToIndonesian(state.selectedDate!!),
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            IconButton(onClick = { viewModel.dismissOrderDialog() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
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
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(
                                    label = "Menu",
                                    value = state.selectedDateOrders.size.toString(),
                                    icon = Icons.Default.ShoppingCart
                                )
                                VerticalDivider(modifier = Modifier.height(36.dp))
                                StatItem(
                                    label = "Qty",
                                    value = state.selectedDateOrders.sumOf { it.quantity }.toString(),
                                    icon = Icons.Default.ShoppingCart
                                )
                                VerticalDivider(modifier = Modifier.height(36.dp))
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
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
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
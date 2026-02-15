package dev.faizal.zypos.ui.screens.transaction

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.faizal.zypos.R
import dev.faizal.zypos.domain.model.order.OrderDetail
import dev.faizal.zypos.domain.model.order.OrderStatus
import dev.faizal.zypos.domain.model.order.PaymentStatus
import dev.faizal.zypos.domain.model.report.DailySalesReport
import dev.faizal.zypos.ui.common.ExportDialog
import dev.faizal.zypos.ui.common.FilterDialog
import dev.faizal.zypos.ui.common.Header
import dev.faizal.zypos.ui.common.MonthYearPickerDialog
import dev.faizal.zypos.ui.common.SortDialog
import dev.faizal.zypos.ui.theme.AccentGreen
import dev.faizal.zypos.ui.utils.ScreenConfig

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
            EmptyTransactionState()
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

    // Order Detail Dialog
    // Di TransactionAllScreen, replace dialog dengan yang ini:
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
                                    value = "Rp ${String.format("%,.0f",
                                        state.selectedDateOrders.sumOf { it.totalPrice })}",
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

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EnhancedOrderMenuItem(order: OrderDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header - Menu name & Status
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
                    // Icon Menu - Ganti jadi Image
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!order.imageUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(order.imageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.snack),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = order.menuName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = order.categoryName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (order.orderStatus) {
                        OrderStatus.COMPLETED -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                        OrderStatus.PENDING -> Color(0xFFFFC107).copy(alpha = 0.15f)
                        OrderStatus.CANCELLED  -> Color(0xFFF44336).copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = order.orderStatus.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (order.orderStatus) {
                            OrderStatus.COMPLETED -> Color(0xFF4CAF50)
                            OrderStatus.PENDING -> Color(0xFFFFC107)
                            OrderStatus.CANCELLED -> Color(0xFFF44336)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Details - Gunakan Image Resources
            Text(
                text = "${order.size} - ${order.temperature} - ${order.orderType}",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer - Customer, Qty & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = order.customerName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Qty: ${order.quantity}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Price
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Rp ${String.format("%,.0f", order.totalPrice)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen
                    )
                    // Payment Status
                    Text(
                        text = order.paymentStatus.name,
                        fontSize = 11.sp,
                        color = if (order.paymentStatus == PaymentStatus.PAID)
                            Color(0xFF4CAF50)
                        else
                            Color(0xFFFFC107),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionSummaryCard(
    totalTransactions: Int,
    totalAmount: Double,
    totalOrders: Int,
    isPhone: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        if (isPhone) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryItem("Total Days", totalTransactions.toString())
                SummaryItem("Total Revenue", "Rp ${String.format("%,.2f", totalAmount)}")
                SummaryItem("Total Orders", totalOrders.toString())
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem("Total Days", totalTransactions.toString())
                VerticalDivider(modifier = Modifier.height(40.dp))
                SummaryItem("Total Revenue", "Rp ${String.format("%,.2f", totalAmount)}")
                VerticalDivider(modifier = Modifier.height(40.dp))
                SummaryItem("Total Orders", totalOrders.toString())
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
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
fun TransactionCard(
    transaction: DailySalesReport,
    onDateClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = transaction.dayOfMonth.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = formatDateToIndonesian(transaction.date),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary, // Biru
                            textDecoration = TextDecoration.Underline, // Garis bawah
                            modifier = Modifier.clickable {
                                onDateClick(transaction.date)
                            }
                        )
                        Text(
                            text = "${transaction.orderCount} Orders",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Rp ${String.format("%,.2f", transaction.totalAmount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen
                )
            }
        }
    }
}

@Composable
fun TransactionTableHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Date",
                modifier = Modifier.weight(1.5f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Day",
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                "Orders",
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                "Total Amount",
                modifier = Modifier.weight(1.2f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
            // Action Header
            Text(
                "Action",
                modifier = Modifier.width(120.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TransactionTableRow(
    transaction: DailySalesReport,
    onDateClick: (String) -> Unit,
    onExportExcel: (DailySalesReport) -> Unit,
    onExportPdf: (DailySalesReport) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date - Clickable dengan underline
        Text(
            text = formatDateToIndonesian(transaction.date),
            modifier = Modifier
                .weight(1.5f)
                .clickable { onDateClick(transaction.date) },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Medium
        )

        // Day
        Text(
            text = "Day ${transaction.dayOfMonth}",
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Orders
        Text(
            text = transaction.orderCount.toString(),
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Total Amount
        Text(
            text = "Rp ${String.format("%,.2f", transaction.totalAmount)}",
            modifier = Modifier.weight(1.2f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AccentGreen,
            textAlign = TextAlign.End
        )

        // Actions - Excel & PDF Icons
        Row(
            modifier = Modifier.width(120.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Excel Icon
            IconButton(
                onClick = { onExportExcel(transaction) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.excel), // Ganti dengan icon excel Anda
                    contentDescription = "Export to Excel",
                    tint = Color(0xFF217346), // Excel green color
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // PDF Icon
            IconButton(
                onClick = { onExportPdf(transaction) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.pdf), // Ganti dengan icon pdf Anda
                    contentDescription = "Export to PDF",
                    tint = Color(0xFFDC3545), // PDF red color
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyTransactionState() {
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
                "No Transactions Found",
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

// Fungsi konversi format tanggal
fun formatDateToIndonesian(date: String): String {
    return try {
        // Dari "14/02/2026" ke "14 Februari 2026"
        val parts = date.split("/")
        val day = parts[0]
        val month = parts[1].toInt()
        val year = parts[2]

        val monthName = when (month) {
            1 -> "Januari"; 2 -> "Februari"; 3 -> "Maret"; 4 -> "April"
            5 -> "Mei"; 6 -> "Juni"; 7 -> "Juli"; 8 -> "Agustus"
            9 -> "September"; 10 -> "Oktober"; 11 -> "November"; 12 -> "Desember"
            else -> ""
        }

        "$day $monthName $year"
    } catch (e: Exception) {
        date // Return original if conversion fails
    }
}

// Helper function
fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
        5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
        9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
        else -> ""
    }
}
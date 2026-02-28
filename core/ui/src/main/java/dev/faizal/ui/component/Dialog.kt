package dev.faizal.ui.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.faizal.core.common.model.SortBy
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dev.faizal.core.designsystem.R


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    startDate: String?,
    endDate: String?,
    onDismiss: () -> Unit,
    onApply: (String?, String?) -> Unit,
    onClear: () -> Unit
) {
    var selectedStartDate by remember { mutableStateOf(startDate) }
    var selectedEndDate by remember { mutableStateOf(endDate) }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = { Text("Filter Transactions") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = selectedStartDate ?: "",
                    onValueChange = { selectedStartDate = it },
                    label = { Text("Start Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("2024-01-01") }
                )

                OutlinedTextField(
                    value = selectedEndDate ?: "",
                    onValueChange = { selectedEndDate = it },
                    label = { Text("End Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("2024-01-31") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onApply(selectedStartDate, selectedEndDate) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onClear) {
                    Text("Clear")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun SortDialog(
    currentSortBy: SortBy,
    isAscending: Boolean,
    onDismiss: () -> Unit,
    onApply: (SortBy, Boolean) -> Unit
) {
    var selectedSortBy by remember { mutableStateOf(currentSortBy) }
    var selectedAscending by remember { mutableStateOf(isAscending) }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = { Text("Sort Transactions") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Sort By:", style = MaterialTheme.typography.titleSmall)

                // Sort options
                SortBy.entries.forEach { sortBy ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedSortBy == sortBy,
                                onClick = { selectedSortBy = sortBy }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSortBy == sortBy,
                            onClick = { selectedSortBy = sortBy }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (sortBy) {
                                SortBy.DATE -> "Date"
                                SortBy.AMOUNT -> "Amount"
                                SortBy.ORDERS -> "Number of Orders"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Text("Order:", style = MaterialTheme.typography.titleSmall)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedAscending,
                            onClick = { selectedAscending = true }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedAscending,
                        onClick = { selectedAscending = true }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ascending (Low to High)")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = !selectedAscending,
                            onClick = { selectedAscending = false }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !selectedAscending,
                        onClick = { selectedAscending = false }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Descending (High to Low)")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApply(selectedSortBy, selectedAscending) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExportExcel: () -> Unit,
    onDownloadPdf: () -> Unit = {},
    onSharePdf: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Image(painter = painterResource(R.drawable.download), contentDescription = null, modifier = Modifier.size(24.dp))
        },
        title = {
            Text("Export Report")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Choose export format and action",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Excel Option
                ExportOptionCard(
                    title = "Excel Spreadsheet",
                    description = "Export data as .xlsx file",
                    icon = R.drawable.excel,
                    onClick = {
                        onExportExcel()
                        onDismiss()
                    }
                )

                // PDF Options
                Text(
                    "PDF Options",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )

                ExportOptionCard(
                    title = "Download PDF",
                    description = "Save to Downloads folder",
                    icon = R.drawable.pdf,
                    onClick = {
                        onDownloadPdf()
                        onDismiss()
                    }
                )

                ExportOptionCard(
                    title = "Share PDF",
                    description = "Share via apps",
                    icon = R.drawable.share,
                    onClick = {
                        onSharePdf()
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ExportOptionCard(
    title: String,
    description: String,
    icon: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter  = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
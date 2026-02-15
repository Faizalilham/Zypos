package dev.faizal.zypos.ui.common

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.faizal.zypos.ui.screens.transaction.SortBy
import java.time.LocalDate

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
    onExportPdf: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.MoreVert, contentDescription = null) },
        title = { Text("Export Transactions") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Choose export format:")

                OutlinedButton(
                    onClick = onExportExcel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export as Excel (.xlsx)")
                }

                OutlinedButton(
                    onClick = onExportPdf,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export as PDF")
                }
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
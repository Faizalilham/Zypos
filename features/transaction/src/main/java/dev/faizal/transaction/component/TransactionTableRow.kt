package dev.faizal.transaction.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.faizal.core.common.utils.formatDateToIndonesian
import dev.faizal.core.designsystem.AccentGreen
import dev.faizal.core.designsystem.R
import dev.faizal.core.domain.model.report.DailySalesReport

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
package dev.faizal.dashboard.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.faizal.core.designsystem.AccentGreen
import dev.faizal.core.domain.model.report.DailySalesReport

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
package dev.faizal.transaction.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
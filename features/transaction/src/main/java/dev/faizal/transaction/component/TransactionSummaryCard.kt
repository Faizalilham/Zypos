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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.faizal.core.common.utils.toCurrencyString

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
            .padding(
                horizontal = if (isPhone) 16.dp else 16.dp,
                vertical = if (isPhone) 8.dp else 8.dp
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = if (isPhone) 12.dp else 16.dp,
                    horizontal = 8.dp
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryItem(
                label = "Total Days",
                value = totalTransactions.toString(),
                isPhone = isPhone
            )
            VerticalDivider(modifier = Modifier.height(36.dp))
            SummaryItem(
                label = "Revenue",
                value = totalAmount.toCurrencyString(),
                isPhone = isPhone
            )
            VerticalDivider(modifier = Modifier.height(36.dp))
            SummaryItem(
                label = "Orders",
                value = totalOrders.toString(),
                isPhone = isPhone
            )
        }
    }
}
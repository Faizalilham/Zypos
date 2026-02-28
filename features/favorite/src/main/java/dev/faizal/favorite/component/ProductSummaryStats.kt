package dev.faizal.favorite.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.faizal.core.common.utils.toRupiahFormatDecimal
import dev.faizal.core.domain.model.report.TopProductReport

@Composable
fun ProductSummaryStats(
    products: List<TopProductReport>,
    isPhone: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        // Selalu Row — HP maupun tablet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductStatItem(
                label = "Products",
                value = products.size.toString(),
                isPhone = isPhone
            )
            VerticalDivider(modifier = Modifier.height(36.dp))
            ProductStatItem(
                label = "Orders",
                value = products.sumOf { it.orderCount }.toString(),
                isPhone = isPhone
            )
            VerticalDivider(modifier = Modifier.height(36.dp))
            ProductStatItem(
                label = "Revenue",
                value = products.sumOf { it.totalAmount }.toRupiahFormatDecimal(),
                isPhone = isPhone
            )
            VerticalDivider(modifier = Modifier.height(36.dp))
            ProductStatItem(
                label = "Avg Orders",
                value = if (products.isEmpty()) "0"
                else "${products.map { it.orderCount }.average().toInt()}",
                isPhone = isPhone
            )
        }
    }
}
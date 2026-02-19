package dev.faizal.favorite.component

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
        if (isPhone) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProductStatItem("Total Products", products.size.toString())
                ProductStatItem(
                    "Total Orders",
                    products.sumOf { it.orderCount }.toString()
                )
                ProductStatItem(
                    "Total Revenue",
                    "Rp ${String.format("%,.0f", products.sumOf { it.totalAmount })}"
                )
                ProductStatItem(
                    "Average Orders",
                    "${products.map { it.orderCount }.average().toInt()}"
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProductStatItem("Total Products", products.size.toString())
                VerticalDivider(modifier = Modifier.height(40.dp))
                ProductStatItem(
                    "Total Orders",
                    products.sumOf { it.orderCount }.toString()
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                ProductStatItem(
                    "Total Revenue",
                    "Rp ${String.format("%,.0f", products.sumOf { it.totalAmount })}"
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                ProductStatItem(
                    "Average Orders",
                    "${products.map { it.orderCount }.average().toInt()}"
                )
            }
        }
    }
}
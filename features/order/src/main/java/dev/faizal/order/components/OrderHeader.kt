package dev.faizal.order.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OrderHeader(
    onMenuClick: () -> Unit,
    isTabletPortrait: Boolean,
    totalOrdersToday: Int = 0
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isTabletPortrait) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Toggle Menu",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onBackground 
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Order List",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground 
                )
                Text(text = " 🚀", fontSize = 24.sp)
            }
            Text(
                text = "total order hari ini: $totalOrdersToday",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant 
            )
        }
    }
}
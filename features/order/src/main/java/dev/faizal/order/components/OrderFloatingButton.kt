package dev.faizal.order.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OrderFloatingButton(
    orderCount: Int,
    onClick: () -> Unit
) {
    Box {
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary 
            )
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = "View Orders",
                tint = MaterialTheme.colorScheme.onPrimary 
            )
        }
        if (orderCount > 0) {
            Badge(
                containerColor = MaterialTheme.colorScheme.error, 
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
            ) {
                Text(
                    orderCount.toString(),
                    color = MaterialTheme.colorScheme.onError 
                )
            }
        }
    }
}
package dev.faizal.order.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.faizal.core.common.utils.toDecimalString
import dev.faizal.core.designsystem.R
import dev.faizal.core.domain.model.order.Order
import dev.faizal.core.domain.model.order.Size
import dev.faizal.core.domain.model.order.Temperature

@Composable
fun OrderItemRow(
    orderItem: Order,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    onEdit: (Order) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product Image — pakai imageUri, fallback ke icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!orderItem.menu.imageUri.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(orderItem.menu.imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = orderItem.menu.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    orderItem.menu.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.size(28.dp)
                ) {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit",
                            modifier = Modifier.size(14.dp), tint = Color(0xFF2196F3))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Size & Temp tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFFF3E0)) {
                    Text(
                        when (orderItem.size) {
                            Size.SMALL -> "S"; Size.MEDIUM -> "M"; Size.LARGE -> "L"
                        },
                        fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFFFF9800),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (orderItem.temperature == Temperature.HOT) Color(0xFFFFEBEE)
                    else Color(0xFFE3F2FD)
                ) {
                    Text(
                        when (orderItem.temperature) {
                            Temperature.HOT -> "🔥 Hot"; Temperature.COLD -> "❄️ Cold"
                        },
                        fontSize = 10.sp, fontWeight = FontWeight.Medium,
                        color = if (orderItem.temperature == Temperature.HOT) Color(0xFFD32F2F)
                        else Color(0xFF1976D2),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quantity selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .width(100.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Surface(shape = CircleShape, color = Color.White) {
                    IconButton(
                        onClick = { onQuantityChange(orderItem.quantity - 1) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("−", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Text(orderItem.quantity.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface)
                Surface(shape = CircleShape, color = Color.White) {
                    IconButton(
                        onClick = { onQuantityChange(orderItem.quantity + 1) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Rp${orderItem.totalPrice.toDecimalString()}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Delete button
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFEE2E2),
            modifier = Modifier.size(32.dp)
        ) {
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(painter = painterResource(R.drawable.delete),
                    contentDescription = "Remove",
                    modifier = Modifier.size(18.dp), tint = Color(0xFFEF4444))
            }
        }
    }

    if (showEditDialog) {
        AddOrderDialog(
            menu = orderItem.menu,
            initialQuantity = orderItem.quantity,
            initialSize = orderItem.size,
            initialTemperature = orderItem.temperature,
            onDismiss = { showEditDialog = false },
            onConfirm = { quantity, size, temperature ->
                onEdit(orderItem.copy(
                    quantity = quantity, size = size, temperature = temperature,
                    totalPrice = calculateTotalPrice(orderItem.menu.basePrice, size, quantity)
                ))
                showEditDialog = false
            }
        )
    }
}

private fun calculateTotalPrice(basePrice: Double, size: Size, quantity: Int): Double {
    val sizeMultiplier = when (size) { Size.SMALL -> 0.8; Size.MEDIUM -> 1.0; Size.LARGE -> 1.3 }
    return (basePrice * sizeMultiplier) * quantity
}
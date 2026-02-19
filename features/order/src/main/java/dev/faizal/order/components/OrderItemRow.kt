package dev.faizal.order.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.faizal.core.domain.model.order.Order
import dev.faizal.core.domain.model.order.Size
import dev.faizal.core.domain.model.order.Temperature
import dev.faizal.core.designsystem.R


@Composable
fun OrderItemRow(
    orderItem: Order,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    onEdit: (Order) -> Unit // TAMBAHKAN INI
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product Image
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Product Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    orderItem.menu.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Edit Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.size(28.dp)
                ) {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF2196F3)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Size & Temperature Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Size Tag
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Text(
                        when (orderItem.size) {
                            Size.SMALL -> "S"
                            Size.MEDIUM -> "M"
                            Size.LARGE -> "L"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Temperature Tag
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (orderItem.temperature == Temperature.HOT)
                        Color(0xFFFFEBEE)
                    else
                        Color(0xFFE3F2FD)
                ) {
                    Text(
                        when (orderItem.temperature) {
                            Temperature.HOT -> "🔥 Hot"
                            Temperature.COLD -> "❄️ Cold"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (orderItem.temperature == Temperature.HOT)
                            Color(0xFFD32F2F)
                        else
                            Color(0xFF1976D2),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quantity Selector
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
                Surface(
                    shape = CircleShape,
                    color = Color.White
                ) {
                    IconButton(
                        onClick = { onQuantityChange(orderItem.quantity - 1) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text(
                            "−",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    orderItem.quantity.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = CircleShape,
                    color = Color.White
                ) {
                    IconButton(
                        onClick = { onQuantityChange(orderItem.quantity + 1) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Increase",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Price
            Text(
                "$${String.format("%.2f", orderItem.totalPrice)}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Delete Button
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFEE2E2),
            modifier = Modifier.size(32.dp)
        ) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.delete),
                    contentDescription = "Remove",
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }

    // Edit Dialog
    if (showEditDialog) {
        AddOrderDialog(
            menu = orderItem.menu,
            initialQuantity = orderItem.quantity,
            initialSize = orderItem.size,
            initialTemperature = orderItem.temperature,
            onDismiss = { showEditDialog = false },
            onConfirm = { quantity, size, temperature ->
                onEdit(
                    orderItem.copy(
                        quantity = quantity,
                        size = size,
                        temperature = temperature,
                        totalPrice = calculateTotalPrice(
                            orderItem.menu.basePrice,
                            size,
                            quantity
                        )
                    )
                )
                showEditDialog = false
            }
        )
    }
}

private fun calculateTotalPrice(
    basePrice: Double,
    size: Size,
    quantity: Int
): Double {
    val sizeMultiplier = when (size) {
        Size.SMALL -> 0.8
        Size.MEDIUM -> 1.0
        Size.LARGE -> 1.3
    }
    return (basePrice * sizeMultiplier) * quantity
}
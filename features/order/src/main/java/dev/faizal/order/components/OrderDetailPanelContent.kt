package dev.faizal.order.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.faizal.core.domain.model.order.Order

@Composable
fun OrderDetailsPanelContent(
    isDineIn: Boolean,
    onDineInChange: (Boolean) -> Unit,
    orderItems: List<Order>,
    onQuantityChange: (Order, Int) -> Unit,
    onRemoveItem: (Order) -> Unit,
    onEditItem: (Order, Order) -> Unit,
    selectedPaymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    onMakeOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subtotal = orderItems.sumOf {
        it.menu.basePrice.toString().replace("$", "").toDoubleOrNull()?.times(it.quantity) ?: 0.0
    }
    val tax = subtotal * 0.10
    val total = subtotal + tax

    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDineInChange(true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDineIn)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface, 
                            contentColor = if (isDineIn)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant 
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Dine in", style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick = { onDineInChange(false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!isDineIn)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface, 
                            contentColor = if (!isDineIn)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant 
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Take away", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (orderItems.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No Item Selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) 
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        DashedDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outlineVariant 
                        )
                    }
                }
            }

            items(orderItems) { orderItem ->
                OrderItemRow(
                    orderItem = orderItem,
                    onQuantityChange = { newQuantity ->
                        if (newQuantity > 0) {
                            onQuantityChange(orderItem, newQuantity)
                        }
                    },
                    onRemove = { onRemoveItem(orderItem) },
                    onEdit = { editedOrder ->
                        onEditItem(orderItem, editedOrder)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))

                DashedDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outlineVariant 
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Payment section
        SerratedContainer(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Subtotal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant 
                )
                Text(
                    "$ ${String.format("%.2f", subtotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface 
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Tax 10%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant 
                )
                Text(
                    "$ ${String.format("%.2f", tax)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface 
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            DashedDivider(color = MaterialTheme.colorScheme.outlineVariant) 
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TOTAL",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface 
                )
                Text(
                    "$ ${String.format("%.2f", total)}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface 
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Payment Method",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface 
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentMethodButton(
                    label = "Credit Card",
                    isSelected = selectedPaymentMethod == "Credit Card",
                    onClick = { onPaymentMethodChange("Credit Card") }
                )
                PaymentMethodButton(
                    label = "Cash",
                    isSelected = selectedPaymentMethod == "Cash",
                    onClick = { onPaymentMethodChange("Cash") }
                )
                PaymentMethodButton(
                    label = "Qris",
                    isSelected = selectedPaymentMethod == "Qris",
                    onClick = { onPaymentMethodChange("Qris") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onMakeOrder,
                modifier = Modifier.fillMaxWidth(),
                enabled = orderItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    "Make Order",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), 
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else
                    Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant 
        )
    }
}

@Composable
fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    dashWidth: Float = 8f,
    dashGap: Float = 8f,
    thickness: Float = 1f
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = thickness,
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(dashWidth, dashGap),
                phase = 0f
            )
        )
    }
}
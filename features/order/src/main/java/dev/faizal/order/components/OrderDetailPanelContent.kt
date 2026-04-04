package dev.faizal.order.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.faizal.core.common.utils.toDecimalString
import dev.faizal.core.domain.model.order.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MAX_VISIBLE_ITEMS = 5

data class TableZone(val zoneName: String, val tables: List<String>)

val dummyTableZones = listOf(
    TableZone("Indoor", (1..10).map { "Meja $it" }),
    TableZone("Outdoor", (11..20).map { "Meja $it" }),
    TableZone("VIP Room", (21..25).map { "Meja $it" })
)

val allTables: List<String> = dummyTableZones.flatMap { zone ->
    listOf("── ${zone.zoneName} ──") + zone.tables
}

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
    selectedTable: String?,
    onTableSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val subtotal = orderItems.sumOf {
        it.menu.basePrice.toString().replace("$", "").toDoubleOrNull()?.times(it.quantity) ?: 0.0
    }
    val tax = subtotal * 0.10
    val total = subtotal + tax

    var showTableDropdown by remember { mutableStateOf(false) }
    var showAllItemsDialog by remember { mutableStateOf(false) }

    val currentDateTime = remember {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy 'pukul' HH:mm", Locale("id", "ID"))
        sdf.format(Date())
    }

    // Tampilkan maks MAX_VISIBLE_ITEMS di list utama
    val visibleItems = if (orderItems.size > MAX_VISIBLE_ITEMS) {
        orderItems.take(MAX_VISIBLE_ITEMS)
    } else {
        orderItems
    }
    val hasMoreItems = orderItems.size > MAX_VISIBLE_ITEMS

    if (showAllItemsDialog) {
        AlertDialog(
            onDismissRequest = { showAllItemsDialog = false },
            title = {
                Text(
                    text = "Semua Item (${orderItems.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    orderItems.forEachIndexed { index, orderItem ->
                        OrderItemRow(
                            orderItem = orderItem,
                            onQuantityChange = { newQuantity ->
                                if (newQuantity > 0) onQuantityChange(orderItem, newQuantity)
                            },
                            onRemove = {
                                onRemoveItem(orderItem)
                                if (orderItems.size - 1 <= MAX_VISIBLE_ITEMS) {
                                    showAllItemsDialog = false
                                }
                            },
                            onEdit = { editedOrder -> onEditItem(orderItem, editedOrder) }
                        )
                        if (index < orderItems.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                            DashedDivider(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllItemsDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Order Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Customer Walk-in",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = currentDateTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onDineInChange(true) },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        border = BorderStroke(
                            width = if (isDineIn) 2.dp else 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isDineIn)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Dine In",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onDineInChange(false)
                            onTableSelected(null)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        border = BorderStroke(
                            width = if (!isDineIn) 2.dp else 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!isDineIn)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Take Away",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    if (isDineIn) {
                        Box {
                            OutlinedButton(
                                onClick = { showTableDropdown = true },
                                modifier = Modifier.height(34.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = selectedTable ?: "Meja",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showTableDropdown,
                                onDismissRequest = { showTableDropdown = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                allTables.forEach { table ->
                                    val isHeader = table.startsWith("──")
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = table,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isHeader)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            if (!isHeader) {
                                                onTableSelected(table)
                                                showTableDropdown = false
                                            }
                                        },
                                        enabled = !isHeader
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Order ID ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "(Baru)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (orderItems.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No Item Selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DashedDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            items(visibleItems) { orderItem ->
                OrderItemRow(
                    orderItem = orderItem,
                    onQuantityChange = { newQuantity ->
                        if (newQuantity > 0) onQuantityChange(orderItem, newQuantity)
                    },
                    onRemove = { onRemoveItem(orderItem) },
                    onEdit = { editedOrder -> onEditItem(orderItem, editedOrder) }
                )
                Spacer(modifier = Modifier.height(8.dp)) // dari 12
                DashedDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tombol "Lihat Semua" jika item > MAX_VISIBLE_ITEMS
            if (hasMoreItems) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = { showAllItemsDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+ ${orderItems.size - MAX_VISIBLE_ITEMS} item lainnya • Lihat Semua",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        // ===================== BOTTOM SECTION =====================
        SerratedContainer(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface
        ) {
            // ---- Subtotal / Discount / Tax: font lebih kecil ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Subtotal",
                    style = MaterialTheme.typography.labelMedium, // dari bodyMedium
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Rp ${subtotal.toDecimalString()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp)) // dari 8

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Discount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Rp 0",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Tax 10%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Rp ${tax.toDecimalString()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp)) // dari 16
            DashedDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // ---- TOTAL ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TOTAL",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), // dari titleMedium
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Rp ${total.toDecimalString()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), // dari titleLarge
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp)) // dari 24

            // ---- Payment Method ----
            Text(
                "Payment Method",
                style = MaterialTheme.typography.bodySmall, // dari titleMedium
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp)) // dari 12

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PaymentMethodButton(
                    label = "Cash",
                    isSelected = selectedPaymentMethod == "Cash",
                    onClick = { onPaymentMethodChange("Cash") }
                )
                PaymentMethodButton(
                    label = "Credit Card",
                    isSelected = selectedPaymentMethod == "Credit Card",
                    onClick = { onPaymentMethodChange("Credit Card") }
                )
                PaymentMethodButton(
                    label = "Qris",
                    isSelected = selectedPaymentMethod == "Qris",
                    onClick = { onPaymentMethodChange("Qris") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp)) // dari 24

            // ---- Print & Clear ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Print */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp), // lebih compact
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Print",
                        style = MaterialTheme.typography.labelMedium, // dari labelLarge
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { orderItems.forEach { onRemoveItem(it) } },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        "Clear",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ===================== PAY BUTTON =====================
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
                contentPadding = PaddingValues(vertical = 12.dp) // dari 16
            ) {
                Text(
                    text = "Bayar Rp ${total.toDecimalString()}",
                    style = MaterialTheme.typography.bodyMedium, // dari titleMedium
                    fontWeight = FontWeight.Bold
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
            .padding(horizontal = 12.dp, vertical = 6.dp) // dari horizontal 16, vertical 10
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall, // dari labelMedium
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
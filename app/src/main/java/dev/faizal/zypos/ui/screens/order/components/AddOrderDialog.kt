package dev.faizal.zypos.ui.screens.order.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.faizal.zypos.R
import dev.faizal.zypos.domain.model.menu.Menu
import dev.faizal.zypos.domain.model.order.Size
import dev.faizal.zypos.domain.model.order.Temperature

@Composable
fun AddOrderDialog(
    menu: Menu,
    initialQuantity: Int = 1, // TAMBAHKAN untuk edit mode
    initialSize: Size = Size.MEDIUM, // TAMBAHKAN untuk edit mode
    initialTemperature: Temperature = Temperature.HOT, // TAMBAHKAN untuk edit mode
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, size: Size, temperature: Temperature) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var selectedSize by remember { mutableStateOf(Size.MEDIUM) }
    var selectedTemperature by remember { mutableStateOf(Temperature.HOT) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Blur Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .blur(10.dp)
                    .clickable(onClick = onDismiss)
            )

            // Dialog Content
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(300)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(300)
                ),
                exit = fadeOut(tween(200)) + scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(200)
                )
            ) {
                AddOrderDialogContent(
                    menu = menu,
                    quantity = quantity,
                    selectedSize = selectedSize,
                    selectedTemperature = selectedTemperature,
                    onQuantityChange = { quantity = it },
                    onSizeChange = { selectedSize = it },
                    onTemperatureChange = { selectedTemperature = it },
                    onDismiss = onDismiss,
                    onConfirm = {
                        onConfirm(quantity, selectedSize, selectedTemperature)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun AddOrderDialogContent(
    menu: Menu,
    quantity: Int,
    selectedSize: Size,
    selectedTemperature: Temperature,
    onQuantityChange: (Int) -> Unit,
    onSizeChange: (Size) -> Unit,
    onTemperatureChange: (Temperature) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(400.dp)
            .heightIn(max = 700.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3))
                    )
                    Text(
                        "Add Order",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF666666)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF5F5F5))

            // Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Product Info
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                menu.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A1A1A)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "$${String.format("%.2f", menu.basePrice)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                        }

                        // Product Image
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF3E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (menu.imageUrl != 0) {
                                Image(
                                    painter = painterResource(id = menu.imageUrl),
                                    contentDescription = menu.name,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    "🍵",
                                    fontSize = 40.sp
                                )
                            }
                        }
                    }
                }

                // Quantity Selector
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Quantity",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF5F5F5))
                            ) {
                                Text(
                                    "−",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF666666)
                                )
                            }

                            Text(
                                quantity.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.widthIn(min = 30.dp),
                                textAlign = TextAlign.Center
                            )

                            IconButton(
                                onClick = { onQuantityChange(quantity + 1) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2196F3))
                            ) {
                                Text(
                                    "+",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Select Size
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Select a Cup",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )

                        Size.entries.forEach { size ->
                            SelectableOption(
                                text = when (size) {
                                    Size.SMALL -> "Small (6oz)"
                                    Size.MEDIUM -> "Medium (8oz)"
                                    Size.LARGE -> "Large (12oz)"
                                },
                                isSelected = selectedSize == size,
                                onClick = { onSizeChange(size) }
                            )
                        }
                    }
                }

                // Temperature Selection
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Temperature",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )

                        Temperature.entries.forEach { temp ->
                            SelectableOption(
                                text = when (temp) {
                                    Temperature.HOT -> "Hot"
                                    Temperature.COLD -> "Cold"
                                },
                                isSelected = selectedTemperature == temp,
                                onClick = { onTemperatureChange(temp) }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }
            }

            // Bottom Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                HorizontalDivider(color = Color(0xFFF5F5F5))

                val totalPrice = calculateTotalPrice(
                    menu.basePrice,
                    selectedSize,
                    quantity
                )

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text(
                        "(\$${String.format("%.2f", totalPrice)}) Add to Order",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectableOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF2196F3).copy(alpha = 0.1f) else Color.Transparent,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF2196F3))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text,
                fontSize = 14.sp,
                color = if (isSelected) Color(0xFF2196F3) else Color(0xFF666666),
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(12.dp),
                        painter = painterResource(R.drawable.check),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
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

    return ((basePrice * sizeMultiplier)) * quantity
}
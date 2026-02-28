package dev.faizal.order.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.faizal.core.common.utils.toDecimalString
import dev.faizal.core.designsystem.R
import dev.faizal.core.domain.model.menu.Menu
import dev.faizal.core.domain.model.order.Size
import dev.faizal.core.domain.model.order.Temperature

@Composable
fun AddOrderDialog(
    menu: Menu,
    initialQuantity: Int = 1,
    initialSize: Size = Size.MEDIUM,
    initialTemperature: Temperature = Temperature.HOT,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, size: Size, temperature: Temperature) -> Unit
) {
    var quantity by remember { mutableIntStateOf(initialQuantity) }
    var selectedSize by remember { mutableStateOf(initialSize) }
    var selectedTemperature by remember { mutableStateOf(initialTemperature) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Blur background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .blur(10.dp)
                    .clickable(onClick = onDismiss)
            )

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.8f, animationSpec = tween(300)),
                exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.8f, animationSpec = tween(200))
            ) {
                // Gunakan fillMaxWidth dengan padding horizontal agar responsif di phone
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(max = 680.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
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
                                    modifier = Modifier.size(8.dp).clip(CircleShape)
                                        .background(Color(0xFF2196F3))
                                )
                                Text("Add Order", fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                            }
                            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close",
                                    tint = Color(0xFF666666))
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF5F5F5))

                        LazyColumn(
                            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(4.dp)) }

                            // Product info
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(menu.name, fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Rp${menu.basePrice.toDecimalString()}", fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
                                    }

                                    // Product image — gunakan imageUri bukan imageUrl
                                    Box(
                                        modifier = Modifier.size(72.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF5F5F5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!menu.imageUri.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(menu.imageUri).crossfade(true).build(),
                                                contentDescription = menu.name,
                                                modifier = Modifier.fillMaxSize()
                                                    .clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text("🍵", fontSize = 36.sp)
                                        }
                                    }
                                }
                            }

                            // Quantity
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Quantity", fontSize = 14.sp, color = Color(0xFF666666))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { if (quantity > 1) quantity-- },
                                            modifier = Modifier.size(32.dp).clip(CircleShape)
                                                .background(Color(0xFFF5F5F5))
                                        ) {
                                            Text("−", fontSize = 18.sp,
                                                fontWeight = FontWeight.Medium, color = Color(0xFF666666))
                                        }
                                        Text(quantity.toString(), fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.widthIn(min = 30.dp),
                                            textAlign = TextAlign.Center)
                                        IconButton(
                                            onClick = { quantity++ },
                                            modifier = Modifier.size(32.dp).clip(CircleShape)
                                                .background(Color(0xFF2196F3))
                                        ) {
                                            Text("+", fontSize = 18.sp,
                                                fontWeight = FontWeight.Medium, color = Color.White)
                                        }
                                    }
                                }
                            }

                            // Size
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Select a Cup", fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A))
                                    Size.entries.forEach { size ->
                                        SelectableOption(
                                            text = when (size) {
                                                Size.SMALL -> "Small (6oz)"
                                                Size.MEDIUM -> "Medium (8oz)"
                                                Size.LARGE -> "Large (12oz)"
                                            },
                                            isSelected = selectedSize == size,
                                            onClick = { selectedSize = size }
                                        )
                                    }
                                }
                            }

                            // Temperature
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Temperature", fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A))
                                    Temperature.entries.forEach { temp ->
                                        SelectableOption(
                                            text = when (temp) {
                                                Temperature.HOT -> "Hot"
                                                Temperature.COLD -> "Cold"
                                            },
                                            isSelected = selectedTemperature == temp,
                                            onClick = { selectedTemperature = temp }
                                        )
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(4.dp)) }
                        }

                        // Bottom button
                        Column(
                            modifier = Modifier.fillMaxWidth().background(Color.White)
                        ) {
                            HorizontalDivider(color = Color(0xFFF5F5F5))
                            val totalPrice = calculateTotalPrice(menu.basePrice, selectedSize, quantity)
                            Button(
                                onClick = {
                                    onConfirm(quantity, selectedSize, selectedTemperature)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth().padding(20.dp).height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                            ) {
                                Text("(Rp${totalPrice.toDecimalString()}) Add to Order",
                                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF2196F3).copy(alpha = 0.1f) else Color.Transparent,
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF2196F3))
        else BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, fontSize = 14.sp,
                color = if (isSelected) Color(0xFF2196F3) else Color(0xFF666666),
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal)
            if (isSelected) {
                Box(
                    modifier = Modifier.size(20.dp).clip(CircleShape)
                        .background(Color(0xFF2196F3)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(modifier = Modifier.size(12.dp),
                        painter = painterResource(R.drawable.check),
                        contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

private fun calculateTotalPrice(basePrice: Double, size: Size, quantity: Int): Double {
    val sizeMultiplier = when (size) {
        Size.SMALL -> 0.8
        Size.MEDIUM -> 1.0
        Size.LARGE -> 1.3
    }
    return (basePrice * sizeMultiplier) * quantity
}
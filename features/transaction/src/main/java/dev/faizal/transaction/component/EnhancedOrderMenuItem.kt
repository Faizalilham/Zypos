package dev.faizal.transaction.component

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import dev.faizal.core.designsystem.AccentGreen
import dev.faizal.core.designsystem.R
import dev.faizal.core.domain.model.order.OrderDetail
import dev.faizal.core.domain.model.order.OrderStatus
import dev.faizal.core.domain.model.order.PaymentStatus

@Composable
fun EnhancedOrderMenuItem(order: OrderDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header - Menu name & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon Menu - Ganti jadi Image
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!order.imageUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(order.imageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.snack),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = order.menuName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = order.categoryName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (order.orderStatus) {
                        OrderStatus.COMPLETED -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                        OrderStatus.PENDING -> Color(0xFFFFC107).copy(alpha = 0.15f)
                        OrderStatus.CANCELLED  -> Color(0xFFF44336).copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = order.orderStatus.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (order.orderStatus) {
                            OrderStatus.COMPLETED -> Color(0xFF4CAF50)
                            OrderStatus.PENDING -> Color(0xFFFFC107)
                            OrderStatus.CANCELLED -> Color(0xFFF44336)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Details - Gunakan Image Resources
            Text(
                text = "${order.size} - ${order.temperature} - ${order.orderType}",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer - Customer, Qty & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = order.customerName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Qty: ${order.quantity}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Price
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Rp ${String.format("%,.0f", order.totalPrice)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen
                    )
                    // Payment Status
                    Text(
                        text = order.paymentStatus.name,
                        fontSize = 11.sp,
                        color = if (order.paymentStatus == PaymentStatus.PAID)
                            Color(0xFF4CAF50)
                        else
                            Color(0xFFFFC107),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}